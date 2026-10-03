package com.releasepilot.audit;

import com.releasepilot.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AuditLogDto>>> getAuditLogs(@RequestParam UUID organizationId) {
        List<AuditLogDto> logs = auditLogService.getAuditLogsByOrganization(organizationId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
