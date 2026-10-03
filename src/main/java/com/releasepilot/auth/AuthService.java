package com.releasepilot.auth;

import com.releasepilot.common.BadRequestException;
import com.releasepilot.common.DuplicateResourceException;
import com.releasepilot.common.ResourceNotFoundException;
import com.releasepilot.organization.Organization;
import com.releasepilot.organization.OrganizationMember;
import com.releasepilot.organization.OrganizationMemberRepository;
import com.releasepilot.organization.OrganizationRepository;
import com.releasepilot.organization.OrganizationRole;
import com.releasepilot.security.JwtTokenProvider;
import com.releasepilot.security.UserPrincipal;
import com.releasepilot.user.User;
import com.releasepilot.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        User savedUser = userRepository.save(user);

        // Automatically create default organization for newly registered user
        String orgSlug = request.getName().toLowerCase().replaceAll("[^a-z0-9]", "-") + "-org-" + UUID.randomUUID().toString().substring(0, 4);
        Organization defaultOrg = Organization.builder()
                .name(request.getName() + "'s Org")
                .slug(orgSlug)
                .build();
        Organization savedOrg = organizationRepository.save(defaultOrg);

        OrganizationMember member = OrganizationMember.builder()
                .user(savedUser)
                .organization(savedOrg)
                .role(OrganizationRole.OWNER)
                .build();
        organizationMemberRepository.save(member);

        String token = tokenProvider.generateTokenFromUserId(savedUser.getId(), savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .user(mapToUserDto(savedUser))
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", principal.getId()));

        String token = tokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .accessToken(token)
                .user(mapToUserDto(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToUserDto(user);
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .avatarUrl(user.getAvatarUrl())
                .githubId(user.getGithubId())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
