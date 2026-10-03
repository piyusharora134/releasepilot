package com.releasepilot.organization;

import com.releasepilot.common.ApiResponse;
import com.releasepilot.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationDto>> createOrganization(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDto org = organizationService.createOrganization(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Organization created successfully", org));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrganizationDto>>> getUserOrganizations(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<OrganizationDto> orgs = organizationService.getUserOrganizations(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(orgs));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<OrganizationDto>> getOrganizationBySlug(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String slug) {
        OrganizationDto org = organizationService.getOrganizationBySlug(principal.getId(), slug);
        return ResponseEntity.ok(ApiResponse.success(org));
    }
}
