package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.utils.Result;

import java.util.List;

public interface IStoreService extends IService<Store> {
    //Pagination Query
    IPage<Store> paginationQueryByName(int pageNo, int pageSize, String storeName);

    //query province info
    List<String> getAllProvince();

    //get store list by province
    List<StoreDTO> getStoreByProvince(String province);

    //get store list by manager id which is logged in
    List<StoreDTO> getStoreListByManagerId();

    //change store
    Result switchStore(String storeIdToSwitch);
}
