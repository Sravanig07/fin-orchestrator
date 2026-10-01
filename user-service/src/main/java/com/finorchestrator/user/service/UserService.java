package com.finorchestrator.user.service;

import com.finorchestrator.user.dto.AuthResponse;
import com.finorchestrator.user.dto.LoginRequest;
import com.finorchestrator.user.dto.RegisterRequest;
import com.finorchestrator.user.dto.UserProfileDto;
import com.finorchestrator.user.dto.UserPreferencesDto;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileDto getUserProfile(Long userId);
    UserPreferencesDto updatePreferences(Long userId, UserPreferencesDto preferencesDto);
}
