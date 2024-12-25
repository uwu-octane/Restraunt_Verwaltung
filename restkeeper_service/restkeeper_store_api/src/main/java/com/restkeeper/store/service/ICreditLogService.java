package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.CreditLogs;

import java.time.LocalDateTime;
import java.util.List;

public interface ICreditLogService extends IService<CreditLogs> {

    IPage<CreditLogs> queryPage(String creditId, int pageNum, int pageSize);

    List<CreditLogs> listCreditLog(String creditId, LocalDateTime start, LocalDateTime end);
}
