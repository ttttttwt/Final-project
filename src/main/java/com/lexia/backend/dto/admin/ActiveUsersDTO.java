package com.lexia.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for active users monitoring response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveUsersDTO {
    private long totalActiveUsers;
    private long desktopUsers;
    private long mobileUsers;
    private long tabletUsers;
    private List<UserSessionDTO> recentSessions;
}
