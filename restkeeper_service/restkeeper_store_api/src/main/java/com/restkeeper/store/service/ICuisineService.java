package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Cuisine;
import com.restkeeper.store.entity.CuisineFlavor;

import java.util.List;
import java.util.Map;

public interface ICuisineService extends IService<Cuisine> {

    //add new cuisine
    boolean addCuisine(Cuisine cuisine, List<CuisineFlavor> cuisineFlavorList);

    List<Map<String,Object>> findAvailableCuisineListInfo(String categoryId, String name);
}
