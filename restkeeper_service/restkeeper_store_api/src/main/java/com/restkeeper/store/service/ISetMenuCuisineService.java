package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Cuisine;
import com.restkeeper.store.entity.SetMenuCuisine;

import java.util.List;

public interface ISetMenuCuisineService extends IService<SetMenuCuisine> {
    List<Cuisine> getAllCuisineBySetMenuId(String dishId);

    Integer getCuisineCopiesInSetMenu(String id, String dishId);
}
