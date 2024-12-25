package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.utils.Result;

import java.util.List;

public interface IStoreManagerService extends IService<StoreManager> {

    IPage<StoreManager> queryPageByCriteria(int pageNo, int pageSize, String criteria);

    //add manager
    boolean addStoreManager(String name, String email, String phone, List<String> storeIds);

    //update manager
    boolean updateStoreManager(String storeManagerId, String name, String phone, String email, List<String> storeIds);

    //logic delete
    boolean logicDeleteStoreManager(String storeManagerId);

    boolean suspendStoreManager(String storeManagerId);

    Result storeManagerLogin(String shopId, String email, String password);

    boolean resetPassword(String storeManagerId, String newPassword);


}
