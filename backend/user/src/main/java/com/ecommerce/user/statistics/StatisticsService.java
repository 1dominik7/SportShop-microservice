package com.ecommerce.user.statistics;

import com.ecommerce.user.clients.ProductCallerService;
import com.ecommerce.user.clients.ShopOrderCallerService;
import com.ecommerce.user.clients.dto.ShopOrderStatisticsResponse;
import com.ecommerce.user.statistics.dto.OrderStatusStatisticsResponse;
import com.ecommerce.user.statistics.dto.StatisticsResponse;
import com.ecommerce.user.statistics.dto.UsersStatistics;
import com.ecommerce.user.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final ProductCallerService productCallerService;
    private final ShopOrderCallerService shopOrderCallerService;

    public StatisticsResponse getOrderStatusStatistics(Jwt jwt) {
        long users = userRepository.count();
        long totalProducts = productCallerService.getTotalProductItemsNumber();
        ShopOrderStatisticsResponse shopOrderStatisticsResponse = shopOrderCallerService.getShopOrderIncomesAndTotalOrders(jwt);

        YearMonth now = YearMonth.now();
        YearMonth lastMonth = now.minusMonths(1);
        YearMonth twoMonthsAgo = now.minusMonths(2);

        Long usersInCurrentMonth = userRepository.countByCreatedDateBetween(
                now.atDay(1).atStartOfDay(),
                now.atEndOfMonth().atTime(23, 59, 59)
        );

        Long usersInLastMonth = userRepository.countByCreatedDateBetween(
                lastMonth.atDay(1).atStartOfDay(),
                lastMonth.atEndOfMonth().atTime(23, 59, 59)
        );

        Long usersTwoMonthsAgo = userRepository.countByCreatedDateBetween(
                twoMonthsAgo.atDay(1).atStartOfDay(),
                twoMonthsAgo.atEndOfMonth().atTime(23, 59, 59)
        );

        return StatisticsResponse.builder()
                .usersStatistics(new UsersStatistics(
                                users,
                                usersInCurrentMonth,
                                usersInLastMonth,
                                usersTwoMonthsAgo
                        )
                )
                .totalProducts(totalProducts)
                .totalIncomes(shopOrderStatisticsResponse.getTotalIncomes())
                .totalOrders(shopOrderStatisticsResponse.getTotalOrders())
                .build();
    }

    public List<OrderStatusStatisticsResponse> getTopOrderStatuses(Jwt jwt) {
        List<OrderStatusStatisticsResponse> topOrderStatuses = shopOrderCallerService.getTopOrderStatuses(jwt);
        return topOrderStatuses;
    }

}
