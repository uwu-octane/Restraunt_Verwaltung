package com.restkeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.dto.DayAmountCollectDTO;
import com.restkeeper.dto.PayTypeCollectDTO;
import com.restkeeper.dto.PrivilegeDTO;
import com.restkeeper.entity.ReportPay;

import java.time.LocalDate;
import java.util.List;

public interface IReportPayService extends IService<ReportPay> {
    List<DayAmountCollectDTO> getDayAmountCollect(LocalDate start, LocalDate end);

    List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start,LocalDate end);

    PrivilegeDTO getPrivilegeCollectByDate(LocalDate start,LocalDate end);
}
