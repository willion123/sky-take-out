package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.service.reportService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
public class reportServiceImpl implements reportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    WorkspaceService workspaceService;

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
        // 创建一个日期列表，包含begin和end之间的所有日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        List<Long> newUserList = new ArrayList<>();
        List<Long> totalUserList = new ArrayList<>();

        // 获取每天新增用户数和总用户数
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

    /* *
     * 订单统计
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        // 创建一个日期列表，包含begin和end之间的所有日期
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        // 获取每天订单数
        List<Integer> orderCountList = new ArrayList<>();
        // 获取每天有效订单数
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            orderCountList.add(orderMapper.getOrderCountByDateRange(
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX),
                    null
            ));
            validOrderCountList.add(orderMapper.getOrderCountByDateRange(
                    LocalDateTime.of(date, LocalTime.MIN),
                    LocalDateTime.of(date, LocalTime.MAX),
                    Orders.COMPLETED
            ));
        }

        return OrderReportVO
                .builder()
                .dateList(dateList.stream().map(LocalDate::toString).collect(Collectors.joining(",")))
                .orderCountList(orderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .validOrderCountList(validOrderCountList.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .totalOrderCount(orderCountList.stream().mapToInt(Integer::intValue).sum())
                .validOrderCount(validOrderCountList.stream().mapToInt(Integer::intValue).sum())
                .orderCompletionRate(orderCountList.stream().mapToInt(Integer::intValue).sum() == 0 ? 0.0 : validOrderCountList.stream().mapToInt(Integer::intValue).sum() * 1.0 / orderCountList.stream().mapToInt(Integer::intValue).sum())
                .build();
    }

    /**
     * 销量排名
     *
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(
                LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX)
        );

        return SalesTop10ReportVO
                .builder()
                .nameList(salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.joining(",")))
                .numberList(salesTop10.stream().map(GoodsSalesDTO::getNumber).map(String::valueOf).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 导出营业数据
     *
     * @param response
     */
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库获取营业数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(
                LocalDateTime.of(LocalDate.now().minusDays(30), LocalTime.MIN),
                LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MAX)
        );
        
        //加载Excel模板文件
        try (XSSFWorkbook workbook = new XSSFWorkbook(Objects.requireNonNull(this.getClass().getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx")))) {
            
            //获取第一个工作表
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            //填写日期范围
            sheet.getRow(1)
                    .getCell(1)
                    .setCellValue("时间：" + LocalDate.now().minusDays(30) + "至" + LocalDate.now().minusDays(1));

            //填写数据
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行（索引为4）
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());
            
            //填充明细数据
            fillDetailData(sheet, LocalDate.now().minusDays(30));

            //将Excel写入响应输出流
            workbook.write(response.getOutputStream());
            

        } catch (IOException e) {
            throw new RuntimeException("导出运营数据失败", e);
        }
    }

    /**
     * 填充明细数据（Stream方式）
     * 
     * @param sheet Excel工作表
     * @param dateBegin 开始日期
     */
    private void fillDetailData(XSSFSheet sheet, LocalDate dateBegin) {
        // 使用Stream处理30天的数据
        IntStream.range(0, 30)
            .mapToObj(dateBegin::plusDays)
            .forEach(date -> {
                // 查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX)
                );

                // 获取对应行并填充数据
                XSSFRow row = sheet.getRow(7 + (int)(date.toEpochDay() - dateBegin.toEpochDay()));
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            });
    }
}