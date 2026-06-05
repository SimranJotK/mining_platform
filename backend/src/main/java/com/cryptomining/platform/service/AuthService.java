package com.cryptomining.platform.service;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.entity.*;
import com.cryptomining.platform.exception.*;
import com.cryptomining.platform.repository.*;
import com.cryptomining.platform.security.JwtTokenProvider;
import com.cryptomining.platform.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EncryptedUserDataRepository encryptedUserDataRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private final MfaService mfaService;
    private final AuditService auditService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new ResourceNotFoundException("Default role not found"));

        User user = User.builder()
            .email(request.getEmail())
            .username(request.getUsername())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .rsaPublicKey(request.getRsaPublicKey())
            .accountStatus(User.AccountStatus.ACTIVE)
            .build();
        user.getRoles().add(userRole);
        user = userRepository.save(user);

        if (request.getEncryptionPassphrase() != null) {
            String salt = encryptionService.generateSalt();
            var result = encryptionService.encryptWithUserKey(
                "{\"initialized\":true}", request.getEncryptionPassphrase(), salt);
            encryptedUserDataRepository.save(EncryptedUserData.builder()
                .user(user)
                .dataType("VAULT_INIT")
                .encryptedPayload(result.encryptedPayload())
                .iv(result.iv())
                .keySalt(result.salt())
                .build());
        }

        auditService.log(user.getId(), "USER_REGISTERED", "USER", user.getId().toString(),
            AuditLog.AuditStatus.SUCCESS, null);

        return login(new AuthRequest() {{
            setEmail(request.getEmail());
            setPassword(request.getPassword());
        }});
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new ForbiddenException("Account suspended");
        }
        if (user.getAccountStatus() == User.AccountStatus.LOCKED) {
            throw new ForbiddenException("Account locked due to failed login attempts");
        }

        if (Boolean.TRUE.equals(user.getMfaEnabled())) {
            if (request.getMfaCode() == null) {
                return AuthResponse.builder().mfaRequired(true).build();
            }
            if (!mfaService.verifyCode(user.getMfaSecret(), request.getMfaCode())) {
                auditService.log(user.getId(), "MFA_FAILED", "AUTH", null,
                    AuditLog.AuditStatus.FAILURE, null);
                throw new UnauthorizedException("Invalid MFA code");
            }
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            String accessToken = tokenProvider.generateAccessToken(auth);
            String refreshToken = tokenProvider.generateRefreshToken(auth);
            saveRefreshToken(user, refreshToken);

            auditService.log(user.getId(), "LOGIN_SUCCESS", "AUTH", null,
                AuditLog.AuditStatus.SUCCESS, null);

            return buildAuthResponse(accessToken, refreshToken, user);
        } catch (BadCredentialsException e) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= 5) {
                user.setAccountStatus(User.AccountStatus.LOCKED);
            }
            userRepository.save(user);
            auditService.log(user.getId(), "LOGIN_FAILED", "AUTH", null,
                AuditLog.AuditStatus.FAILURE, null);
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String tokenHash = hashToken(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (stored.getRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            user.getEmail(), null,
            user.getRoles().stream()
                .map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList()));

        String newAccess = tokenProvider.generateAccessToken(auth);
        String newRefresh = tokenProvider.generateRefreshToken(auth);
        saveRefreshToken(user, newRefresh);

        return buildAuthResponse(newAccess, newRefresh, user);
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        refreshTokenRepository.deleteByUser(user);
        auditService.log(userId, "LOGOUT", "AUTH", null, AuditLog.AuditStatus.SUCCESS, null);
    }

    private void saveRefreshToken(User user, String token) {
        refreshTokenRepository.save(RefreshToken.builder()
            .user(user)
            .tokenHash(hashToken(token))
            .expiresAt(LocalDateTime.now().plusDays(7))
            .build());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Token hashing failed", e);
        }
    }

    private AuthResponse buildAuthResponse(String access, String refresh, User user) {
        Set<String> roles = user.getRoles().stream()
            .map(Role::getName).collect(Collectors.toSet());

        return AuthResponse.builder()
            .accessToken(access)
            .refreshToken(refresh)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getExpirationMs() / 1000)
            .mfaRequired(false)
            .user(UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles)
                .mfaEnabled(Boolean.TRUE.equals(user.getMfaEnabled()))
                .build())
            .build();
    }
}
