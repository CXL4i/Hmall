package com.hmall.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;
@Slf4j
@RequiredArgsConstructor
public class RabbitMqHelper {

    private final RabbitTemplate rabbitTemplate;
    public void sendConfirmMessage(String exchange, String routingKey, Object message) {
        CorrelationData cd = new CorrelationData(UUID.randomUUID().toString());
        cd.getFuture().addCallback(new ListenableFutureCallback<CorrelationData.Confirm>() {
            @Override
            public void onFailure(Throwable ex) {
                log.info("消息发送失败:{}", ex.getMessage());
            }
            @Override
            public void onSuccess(CorrelationData.Confirm result) {
                if (result!=null&& result.isAck()) {
                    log.info("消息发送成功");
                } else {
                    log.info("消息发送失败");
                }
            }
        });
        rabbitTemplate.convertAndSend(exchange, routingKey, message, cd);
    }
}
