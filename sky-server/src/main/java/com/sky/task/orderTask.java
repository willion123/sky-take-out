package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Component
@Slf4j
public class orderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时任务，处理订单支付超时
     */
    @Scheduled(cron = "0 * * * * ?")//每分钟执行一次
    public void possibleTimeoutOrder(){
        log.info("处理超时订单;{}", LocalDateTime.now());

        List<Orders> orders =orderMapper.getOrderTimeout(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(-15));

        if (orders != null && !orders.isEmpty()){
            for (Orders order : orders) {
                orderMapper.update(Orders.builder()
                        .id(order.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("支付超时")
                        .cancelTime(LocalDateTime.now())
                        .build());
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processCompletedOrder(){
        log.info("处理待派单的订单:{}", LocalDateTime.now());
        List<Orders> orders = orderMapper.getOrderTimeout(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusHours(1));
        if (orders != null && !orders.isEmpty()){
            for (Orders order : orders) {
                orderMapper.update(Orders.builder()
                        .id(order.getId())
                        .status(Orders.DELIVERY_IN_PROGRESS)
                        .build());
            }
        }
    }
}
