package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.CreditRepayment;
import org.springframework.transaction.annotation.Transactional;

public interface ICreditRepaymentService extends IService<CreditRepayment> {
    @Transactional
    boolean repayment(CreditRepayment creditRepayment);
}
