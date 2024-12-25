package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.restkeeper.store.entity.Cuisine;
import com.restkeeper.store.entity.SetMenuCuisine;
import com.restkeeper.store.mapper.SetMenuCuisineMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;


@Service(version = "1.0.0",protocol = "dubbo")
@org.springframework.stereotype.Service("setMenuCuisineService")
public class SetMenuCuisineServiceImpl extends ServiceImpl<SetMenuCuisineMapper, SetMenuCuisine> implements ISetMenuCuisineService{

    @Autowired
    @Qualifier("cuisineService")
    private ICuisineService cuisineService;

    @Override
    public List<Cuisine> getAllCuisineBySetMenuId(String setMenuId) {
        List<SetMenuCuisine> dishList = this.getBaseMapper().selectDishes(setMenuId);
        List<String> dishIds = Lists.newArrayList();
        dishList.forEach(setMealDish -> {
            dishIds.add(setMealDish.getDishId());
        });

        return cuisineService.listByIds(dishIds);
    }

    @Override
    public Integer getCuisineCopiesInSetMenu(String cuisineId, String setMenuId) {
        QueryWrapper<SetMenuCuisine> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(SetMenuCuisine::getDishId,cuisineId).eq(SetMenuCuisine::getSetMealId,setMenuId);
        SetMenuCuisine setMealDish = this.getOne(wrapper);
        return setMealDish==null?0:setMealDish.getDishCopies();
    }
}
