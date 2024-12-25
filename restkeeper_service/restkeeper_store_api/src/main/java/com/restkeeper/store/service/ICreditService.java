package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;

import java.util.List;

public interface ICreditService extends IService<Credit> {

    boolean add(Credit credit, List<CreditCompanyUser> users);

    IPage<Credit> queryPage(int pageNum, int pageSize, String username);

    Credit queryById(String id);

    boolean updateCredit(Credit credit, List<CreditCompanyUser> users);
}
