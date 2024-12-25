package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.utils.Result;

public interface IEnterpriseAccountService extends IService<EnterpriseAccount> {
    //page query (according to the enterprise name, fuzzy query)
    IPage<EnterpriseAccount> queryPageByName(int pageNum, int pageSize, String enterpriseName);

    //add new account
    boolean add(EnterpriseAccount enterpriseAccount);

    //recovery del account
    boolean recovery(String id);

    boolean isExist(String id);

    boolean restPassword(String id, String password);

    //enterprise login
    Result login(String shopId, String email, String loginPass);
}
