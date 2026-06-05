package com.cryptomining.platform.controller;

import com.cryptomining.platform.dto.*;
import com.cryptomining.platform.security.UserPrincipal;
import com.cryptomining.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(userService.getProfile(principal.getId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(
            principal.getId(), body.get("firstName"), body.get("lastName"))));
    }

    @PostMapping("/encrypted-data")
    public ResponseEntity<ApiResponse<Void>> storeEncryptedData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        userService.storeEncryptedData(principal.getId(),
            body.get("dataType"), body.get("data"), body.get("passphrase"));
        return ResponseEntity.ok(ApiResponse.success("Data encrypted and stored", null));
    }

    @PostMapping("/encrypted-data/retrieve")
    public ResponseEntity<ApiResponse<String>> retrieveEncryptedData(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String data = userService.retrieveEncryptedData(principal.getId(),
            body.get("dataType"), body.get("passphrase"));
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PostMapping("/mfa/enable")
    public ResponseEntity<ApiResponse<Map<String, String>>> enableMfa(
            @AuthenticationPrincipal UserPrincipal principal) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(userService.enableMfa(principal.getId())));
    }

    @PostMapping("/mfa/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmMfa(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        userService.confirmMfa(principal.getId(), body.get("code"));
        return ResponseEntity.ok(ApiResponse.success("MFA enabled", null));
    }
}
