package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.UserResponse;
import com.cryptomining.platform.entity.*;
import com.cryptomining.platform.exception.*;
import com.cryptomining.platform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EncryptedUserDataRepository encryptedUserDataRepository;
    private final EncryptionService encryptionService;
    private final MfaService mfaService;
    private final AuditService auditService;

    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, String firstName, String lastName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        userRepository.save(user);
        auditService.log(userId, "PROFILE_UPDATED", "USER", userId.toString(),
            AuditLog.AuditStatus.SUCCESS, null);
        return toResponse(user);
    }

    @Transactional
    public void storeEncryptedData(Long userId, String dataType, String plaintext, String passphrase) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String salt = encryptionService.generateSalt();
        var result = encryptionService.encryptWithUserKey(plaintext, passphrase, salt);

        EncryptedUserData data = encryptedUserDataRepository
            .findByUserIdAndDataType(userId, dataType)
            .orElse(EncryptedUserData.builder().user(user).dataType(dataType).build());

        data.setEncryptedPayload(result.encryptedPayload());
        data.setIv(result.iv());
        data.setKeySalt(result.salt());
        encryptedUserDataRepository.save(data);

        auditService.log(userId, "ENCRYPTED_DATA_STORED", "ENCRYPTED_DATA", dataType,
            AuditLog.AuditStatus.SUCCESS, null);
    }

    public String retrieveEncryptedData(Long userId, String dataType, String passphrase) {
        EncryptedUserData data = encryptedUserDataRepository
            .findByUserIdAndDataType(userId, dataType)
            .orElseThrow(() -> new ResourceNotFoundException("Encrypted data not found"));
        return encryptionService.decryptWithUserKey(
            data.getEncryptedPayload(), data.getIv(), passphrase, data.getKeySalt());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void suspendUser(Long adminId, Long targetUserId) {
        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (target.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_CREATOR"))) {
            throw new ForbiddenException("Cannot suspend creator accounts");
        }
        target.setAccountStatus(User.AccountStatus.SUSPENDED);
        userRepository.save(target);
        auditService.log(adminId, "USER_SUSPENDED", "USER", targetUserId.toString(),
            AuditLog.AuditStatus.SUCCESS, null);
    }

    @Transactional
    public void activateUser(Long adminId, Long targetUserId) {
        User target = userRepository.findById(targetUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        target.setAccountStatus(User.AccountStatus.ACTIVE);
        target.setFailedLoginAttempts(0);
        userRepository.save(target);
        auditService.log(adminId, "USER_ACTIVATED", "USER", targetUserId.toString(),
            AuditLog.AuditStatus.SUCCESS, null);
    }

    @Transactional
    public Map<String, String> enableMfa(Long userId) throws Exception {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String secret = mfaService.generateSecret();
        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);

        String qrUrl = mfaService.generateQrCodeUrl(user.getEmail(), secret);
        return Map.of(
            "secret", secret,
            "qrCodeUrl", qrUrl,
            "qrCodeBase64", mfaService.generateQrCodeBase64(qrUrl)
        );
    }

    @Transactional
    public void confirmMfa(Long userId, String code) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!mfaService.verifyCode(user.getMfaSecret(), code)) {
            throw new BadRequestException("Invalid MFA code");
        }
        user.setMfaEnabled(true);
        userRepository.save(user);
        auditService.log(userId, "MFA_ENABLED", "USER", userId.toString(),
            AuditLog.AuditStatus.SUCCESS, null);
    }

    public long countActiveUsers() {
        return userRepository.countByAccountStatus(User.AccountStatus.ACTIVE);
    }

    public long countTotalUsers() {
        return userRepository.count();
    }

    private UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
            .map(Role::getName).collect(Collectors.toSet());
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .roles(roles)
            .accountStatus(user.getAccountStatus().name())
            .mfaEnabled(Boolean.TRUE.equals(user.getMfaEnabled()))
            .lastLoginAt(user.getLastLoginAt())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
