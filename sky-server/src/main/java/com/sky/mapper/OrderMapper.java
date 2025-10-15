package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 修改订单状态为4（支付超时）
     * @param pendingPayment
     * @param localDateTime
     */
    @Select("select * from orders where status = #{pendingPayment} and order_time < #{localDateTime}")
    List<Orders> getOrderTimeout(Integer pendingPayment, LocalDateTime localDateTime);

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);
    
    /**
     * 根据日期范围和状态查询营业额
     * @param begin
     * @param end
     * @param status
     * @return
     */
    @Select("select sum(amount) from orders where status = #{status} and order_time >= #{begin} and order_time <= #{end}")
    Double getTurnoverSumByDateRange(LocalDateTime begin, LocalDateTime end, Integer status);

    /**
     * 根据日期范围查询订单数量
     * @param begin
     * @param end
     * @return
     */
    Integer getOrderCountByDateRange(LocalDateTime begin, LocalDateTime end , Integer status);

    /**
     * 查询销量排名top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}