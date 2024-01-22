package com.hmall.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "pay-service")
public interface PayClient {
    @GetMapping("pay-orders/status/{bizOrderNo}")
    Integer queryPayOrderStatus(@PathVariable Long bizOrderNo);
}
