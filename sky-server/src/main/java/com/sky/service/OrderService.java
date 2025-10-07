package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO);
}
