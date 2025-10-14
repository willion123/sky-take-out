package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.reportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class reportServiceImpl implements reportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        // 创建一个日期列表，包含begin和end之间的所有日期
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }


        // 查询每天的营业额
        List<Double> turnoverList = dateList.stream().map(date ->
                orderMapper.getTurnoverSumByDateRange(
                        LocalDateTime.of(date, LocalTime.MIN), 
                        LocalDateTime.of(date, LocalTime.MAX),
                        Orders.COMPLETED)
        ).map(turnover -> turnover == null ? 0.0 : turnover) // 处理null值，如果为null则设为0.0
         .collect(Collectors.toList());

        return TurnoverReportVO
                .builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .turnoverList(turnoverList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Long> newUserList = new ArrayList<>();
        List<Long> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            newUserList.add((Long) userMapper.getUserSumByDateRange(
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX)
            ));
            totalUserList.add((Long) userMapper.getUserSumByDateRange(
                    LocalDateTime.of(date, LocalTime.MIN),
                    null
            ));
        }

        return UserReportVO
                .builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .totalUserList(totalUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .newUserList(newUserList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }
}