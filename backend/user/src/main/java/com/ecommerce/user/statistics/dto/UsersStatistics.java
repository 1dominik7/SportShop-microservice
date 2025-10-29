package com.ecommerce.user.statistics.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsersStatistics {
    private Long totalUsers;
    private Long usersInCurrentMonth;
    private Long usersInLastMonth;
    private Long usersTwoMonthsAgo;

}
