package com.restkeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.entity.ReportDishEntity;

import java.time.LocalDate;
import java.util.List;

public interface IReportDishService extends IService<ReportDishEntity> {

    List<ReportDishEntity> getCategoryAmountCollect(LocalDate start,LocalDate end);

    List<ReportDishEntity> getDishRank(LocalDate start,LocalDate end);
}
