package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.entity.Cuisine;
import com.restkeeper.store.entity.CuisineFlavor;
import com.restkeeper.store.mapper.CuisineMapper;

import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service(version = "1.0.0", protocol = "dubbo")
@org.springframework.stereotype.Service("cuisineService")
public class CuisineServiceImpl extends ServiceImpl<CuisineMapper, Cuisine> implements ICuisineService{

    @Autowired
    @Qualifier("cuisineFlavorService")
    private ICuisineFlavorService cuisineFlavorService;


    @Override
    @Transactional
    public boolean addCuisine(Cuisine cuisine, List<CuisineFlavor> cuisineFlavorList) {

        try {
            /* add basic info */
            this.save(cuisine);

            //add relationship with flavor
            cuisineFlavorList.forEach((flavor) ->{flavor.setDishId(cuisine.getId());});
            cuisineFlavorService.saveBatch(cuisineFlavorList);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> findAvailableCuisineListInfo(String categoryId, String name) {
        QueryWrapper<Cuisine> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(Cuisine::getId,Cuisine::getName,Cuisine::getStatus,Cuisine::getPrice);

        if (StringUtils.isNotEmpty(categoryId)){
            queryWrapper.lambda().eq(Cuisine::getCategoryId,categoryId);
        }
        if (StringUtils.isNotEmpty(name)){
            queryWrapper.lambda().eq(Cuisine::getName,name);
        }

        queryWrapper.lambda().eq(Cuisine::getStatus, SystemCode.ENABLED);

        return this.listMaps(queryWrapper);
    }
}
