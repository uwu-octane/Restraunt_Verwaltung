package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import com.restkeeper.store.entity.CuisineCategory;

import java.util.List;
import java.util.Map;

public interface ICuisineCategoryService extends IService<CuisineCategory> {

    //add new category
    boolean add(String name, int type);

    boolean updateCategoryName(String categoryId, String newCategoryName);

    IPage<CuisineCategory> queryCategoryPage(int pageNum, int pageSize);

    List<Map<String, Object>> listCategory(Integer type);
}
