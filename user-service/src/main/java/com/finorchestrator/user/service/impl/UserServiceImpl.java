package com.finorchestrator.user.service.impl;

import com.finorchestrator.common.event.DomainEvent;
import com.finorchestrator.common.event.UserCreatedEvent;
import com.finorchestrator.common.exception.ApiException;
import com.finorchestrator.common.exception.ResourceNotFoundException;
import com.finorchestrator.common.exception.UnauthorizedException;
import com.finorchestrator.common.security.JwtUtils;
import com.finorchestrator.user.dto.*;
import com.finorchestrator.user.entity.User;
import com.finorchestrator.user.entity.UserPreference;
import com.finorchestrator.user.repository.UserPreferenceRepository;
import com.finorchestrator.user.repository.UserRepository;
import com.finorchestrator.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String USERS_TOPIC = "finorchestrator.users.v1";

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Email is already registered", HttpStatus.CONFLICT, "DUPLICATE_EMAIL");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ApiException("Username is already taken", HttpStatus.CONFLICT, "DUPLICATE_USERNAME");
        }

        User user = User.builder()
                .username(request.getUsername().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role("ROLE_USER")
                .status("ACTIVE")
                .build();

        user = userRepository.save(user);

        UserPreference preference = UserPreference.builder()
                .userId(user.getId())
                .currency("INR")
                .monthlyBudget(new BigDecimal("80000.00"))
                .riskTolerance("MODERATE")
                .savingsTargetPercentage(new BigDecimal("20.00"))
                .build();

        preferenceRepository.save(preference);

        // Publish event to Kafka
        publishUserCreatedEvent(user);

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new UnauthorizedException("User account is inactive or suspended");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(null);

        UserPreferencesDto prefDto = preference != null ? UserPreferencesDto.builder()
                .currency(preference.getCurrency())
                .monthlyBudget(preference.getMonthlyBudget())
                .riskTolerance(preference.getRiskTolerance())
                .savingsTargetPercentage(preference.getSavingsTargetPercentage())
                .build() : null;

        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .preferences(prefDto)
                .build();
    }

    @Override
    @Transactional
    public UserPreferencesDto updatePreferences(Long userId, UserPreferencesDto dto) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> UserPreference.builder().userId(userId).build());

        preference.setCurrency(dto.getCurrency());
        preference.setMonthlyBudget(dto.getMonthlyBudget());
        preference.setRiskTolerance(dto.getRiskTolerance());
        preference.setSavingsTargetPercentage(dto.getSavingsTargetPercentage());

        preference = preferenceRepository.save(preference);

        return UserPreferencesDto.builder()
                .currency(preference.getCurrency())
                .monthlyBudget(preference.getMonthlyBudget())
                .riskTolerance(preference.getRiskTolerance())
                .savingsTargetPercentage(preference.getSavingsTargetPercentage())
                .build();
    }

    private void publishUserCreatedEvent(User user) {
        try {
            UserCreatedEvent eventPayload = UserCreatedEvent.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .build();

            DomainEvent<UserCreatedEvent> event = DomainEvent.of("USER_CREATED", "user-service", eventPayload);
            kafkaTemplate.send(USERS_TOPIC, String.valueOf(user.getId()), event);
            log.info("Published USER_CREATED event for userId: {}", user.getId());
        } catch (Exception e) {
            log.warn("Failed to publish USER_CREATED event to Kafka (continuing gracefully): {}", e.getMessage());
        }
    }
}
