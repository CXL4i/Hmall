package com.hmall.cart.listeners;

import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class ClearListener {

    private final ICartService cartService;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "cart.clear.queue", durable = "true"),
            exchange = @Exchange(value = "trade.topic"),
            key = "cart.clear"
    ))
    public void listenClearCart(Collection<Long> itemIds) {
        cartService.removeByItemIds(itemIds);
        System.out.println("清空购物车");
    }
}
