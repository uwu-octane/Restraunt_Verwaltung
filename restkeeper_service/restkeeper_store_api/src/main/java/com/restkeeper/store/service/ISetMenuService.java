package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.SetMenu;
import com.restkeeper.store.entity.SetMenuCuisine;

import java.util.List;

public interface ISetMenuService extends IService<SetMenu> {

    IPage<SetMenu> queryPage(int pageNum, int pageSize, String name);

    boolean addMenu(SetMenu setMenu, List<SetMenuCuisine> setMenuCuisineList);

    boolean update(SetMenu setMenu, List<SetMenuCuisine> setMealDishes);
}