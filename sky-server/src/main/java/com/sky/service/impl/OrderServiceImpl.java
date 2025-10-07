package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.Result;

import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional
    public Result<OrderSubmitVO> submit(OrdersSubmitDTO ordersSubmitDTO) {
        //查询当前用户的地址
        AddressBook byAddressBookId = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (byAddressBookId == null) {
            //用户没有地址, 不能下单
            throw  new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前用户的购物车数据
        ShoppingCart shoppingCart = new ShoppingCart();
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list ==  null || list.isEmpty()) {
            //购物车为空, 不能下单
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入数据（一条数据）
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(currentId);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setOrderTime(LocalDateTime.now());
        orders.setPhone(byAddressBookId.getPhone());
        orders.setConsignee(byAddressBookId.getConsignee());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orderMapper.insert(orders);

        //向订单明细表插入数据（多条数据）
        List<OrderDetail> orderDetailList = list.stream().map(x -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(x, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());

        orderDetailMapper.insertBatch(orderDetailList);

        //清空当前用户的购物车数据
        shoppingCartMapper.delete(currentId);

        //封装VO并返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return Result.success(orderSubmitVO);
    }
}