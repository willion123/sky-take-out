package com.sky.service;

import com.sky.vo.TurnoverReportVO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface reportService {

    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

}
