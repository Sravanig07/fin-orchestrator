package com.finorchestrator.user.controller;

import com.finorchestrator.common.dto.ApiResponse;
import com.finorchestrator.common.exception.UnauthorizedException;
import com.finorchestrator.common.security.SecurityHeaders;
import com.finorchestrator.common.security.UserContext;
import com.finorchestrator.user.dto.UserProfileDto;
import com.finorchestrator.user.dto.UserPreferencesDto;
import com.finorchestrator.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUserProfile(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("User ID missing from request context");
        }
        UserProfileDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserProfile(@PathVariable("id") Long id) {
        UserProfileDto profile = userService.getUserProfile(id);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferencesDto>> updatePreferences(
            @RequestHeader(value = SecurityHeaders.USER_ID_HEADER, required = false) Long headerUserId,
            @Valid @RequestBody UserPreferencesDto preferencesDto) {
        Long userId = headerUserId != null ? headerUserId : UserContext.getUserId();
        if (userId == null) {
            throw new UnauthorizedException("User ID missing from request context");
        }
        UserPreferencesDto updated = userService.updatePreferences(userId, preferencesDto);
        return ResponseEntity.ok(ApiResponse.ok("Preferences updated successfully", updated));
    }
}
