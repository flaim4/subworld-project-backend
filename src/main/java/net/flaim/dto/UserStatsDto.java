package net.flaim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private long totalUsers;
    private long verifiedUsers;
    private long unverifiedUsers;
    private long usersToday;
    private long usersThisWeek;
    private long usersThisMonth;
    private long activeUsers;
    private long inactiveUsers;
}