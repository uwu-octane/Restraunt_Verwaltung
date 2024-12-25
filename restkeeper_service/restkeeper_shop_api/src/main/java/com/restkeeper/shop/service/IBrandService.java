package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.shop.entity.Brand;

import java.util.List;
import java.util.Map;

public interface IBrandService extends IService<Brand> {

    //Pagination Query
    IPage<Brand> paginationQuery(int pageNo, int pageSize);

    //query brand in list
    List<Map<String, Object>> getBrandList();
}
