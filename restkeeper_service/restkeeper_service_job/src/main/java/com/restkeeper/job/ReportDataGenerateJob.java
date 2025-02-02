package com.restkeeper.job;


import com.restkeeper.service.OrderHistoryService;
import com.restkeeper.service.ReportDishService;
import com.restkeeper.service.ReportPayService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class ReportDataGenerateJob {

    @Autowired
    private OrderHistoryService orderHistoryService;

    @Autowired
    private ReportPayService reportPayService;

    @Autowired
    private ReportDishService reportDishService;


    @XxlJob("generateReportHandler")
    public ReturnT<String>  jobHandler(String param){
        //统计历史汇总数据
        reportPayService.generateData();
        System.out.println(LocalDateTime.now()+"********历史汇总成功*********");

        //统计菜品销量数据
        reportDishService.generateData();
        System.out.println(LocalDateTime.now()+"********菜品销量汇总成功*********");

        //调用方法迁移方法，迁移数据
        orderHistoryService.exportToHistory();
        System.out.println(LocalDateTime.now() + "********job执行成功*************");

        return ReturnT.SUCCESS;
    }

}
