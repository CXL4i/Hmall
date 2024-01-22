package com.hmall.trade.listeners;

import com.hmall.api.client.PayClient;
import com.hmall.common.constants.MqConstants;
import com.hmall.common.domain.MultiDelayMessage;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class OrderStatusCheckListener {

    private final IOrderService orderService;
    private final RabbitTemplate rabbitTemplate;
    private final PayClient payClient;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MqConstants.DELAY_QUEUE, durable = "true"),
            exchange = @Exchange(name = MqConstants.DELAY_EXCHANGE, type = "topic"),
            key = MqConstants.DELAY_KEY
    ))
    public void listenOrderDelayMessage(MultiDelayMessage<Long> msg) {
        // 1.查询订单状态
        Order order = orderService.getById(msg.getData());
        // 2.判断是否已支付
        if(order == null || order.getStatus() == 2) {
            return;
        }
        // 3.去支付服务查询支付状态
        Integer status = payClient.queryPayOrderStatus(order.getId());
        // 3.1 已支付，标记订单状态
        if (status == 3) {
            orderService.lambdaUpdate()
                    .set(Order::getStatus, 2)
                    .set(Order::getPayTime, LocalDateTime.now())
                    .eq(Order::getStatus, 1)
                    .eq(Order::getId, msg.getData())
                    .update();
            return;
        }
        if(msg.hasNextDelayTime())
        {
            // 3.2 未支付，重发消息
            rabbitTemplate.convertAndSend(MqConstants.DELAY_EXCHANGE, MqConstants.DELAY_KEY, msg.getData(),message -> {
                message.getMessageProperties().setDelay(msg.getNextDelayTime());
                return message;
            });
            return;
        }
        // 4.未支付，重发或取消
        orderService.cancelOrder(msg.getData());
    }
}
