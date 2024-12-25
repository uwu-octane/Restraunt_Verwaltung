package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.SellCalculation;

public interface ISellCalculationService extends IService<SellCalculation> {
    //get rest cuisine num by id
    Integer getRemainCuisineNum(String cuisineId);

    void decrease(String dishId, Integer dishNumber);

    void add(String dishId, int i);

}
