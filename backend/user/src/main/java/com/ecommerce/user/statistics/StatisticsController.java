package com.ecommerce.user.statistics;

import com.ecommerce.user.statistics.dto.OrderStatusStatisticsResponse;
import com.ecommerce.user.statistics.dto.StatisticsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping()
    public ResponseEntity<StatisticsResponse> getOrdersStatus(@AuthenticationPrincipal Jwt jwt) {

        StatisticsResponse orderStatusResponse = statisticsService.getOrderStatusStatistics(jwt);
        return ResponseEntity.ok(orderStatusResponse);
    }

    @GetMapping("/orderStatus")
    public ResponseEntity<List<OrderStatusStatisticsResponse>> getTopOrderStatuses(@AuthenticationPrincipal Jwt jwt) {

        List<OrderStatusStatisticsResponse> orderStatusResponse = statisticsService.getTopOrderStatuses(jwt);
        return ResponseEntity.ok(orderStatusResponse);
    }
}
