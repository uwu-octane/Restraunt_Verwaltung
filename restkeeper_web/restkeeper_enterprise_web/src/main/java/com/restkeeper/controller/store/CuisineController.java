package com.restkeeper.controller.store;


import com.restkeeper.store.entity.Cuisine;
import com.restkeeper.store.entity.CuisineFlavor;
import com.restkeeper.store.service.ICuisineService;
import com.restkeeper.vo.store.CuisineFlavorVO;
import com.restkeeper.vo.store.CuisineVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = {"Cuisine Management"})
@RestController
@RequestMapping("/cuisine")
public class CuisineController {
    @Reference(version = "1.0.0", check = false)
    private ICuisineService cuisineService;

    @ApiOperation("add new cuisine")
    @PostMapping("/addCuisine")
    public boolean addCuisine(@RequestBody CuisineVO cuisineVO){

        //set cuisine value
        Cuisine cuisine = new Cuisine();
        BeanUtils.copyProperties(cuisineVO,cuisine);

        //set flavor value
        List<CuisineFlavorVO> dishFlavorsVO = cuisineVO.getDishFlavors();
        List<CuisineFlavor> cuisineFlavorList = new ArrayList<>();

        for (CuisineFlavorVO cuisineFlavorVO : dishFlavorsVO){
            CuisineFlavor cuisineFlavor = new CuisineFlavor();
            cuisineFlavor.setFlavorName(cuisineFlavorVO.getFlavor());
            cuisineFlavor.setFlavorValue(cuisineFlavorVO.getFlavorData().toString());
            cuisineFlavorList.add(cuisineFlavor);
        }

        return cuisineService.addCuisine(cuisine, cuisineFlavorList);
    }

    @ApiOperation("query cuisine info by id")
    @GetMapping("/{id}")
    public CuisineVO getCuisineById(@PathVariable("id") String id){
        CuisineVO cuisineVO = new CuisineVO();
        //basic info
        Cuisine cuisine = cuisineService.getById(id);
        if (cuisine == null) {
            throw  new RuntimeException("cuisine not exist");
        }
        BeanUtils.copyProperties(cuisine,cuisineVO);

        List<CuisineFlavorVO> cuisineFlavorVOList = new ArrayList<>();

        List<CuisineFlavor> cuisineFlavors = cuisine.getFlavorList();

        for (CuisineFlavor cuisineFlavor : cuisineFlavors){
            CuisineFlavorVO  cuisineFlavorVO = new CuisineFlavorVO();
            cuisineFlavorVO.setFlavor(cuisineFlavor.getFlavorName());
            String flavorValue = cuisineFlavor.getFlavorValue();
            //处理字符串数组
            String quflavorValue = flavorValue.substring(flavorValue.indexOf("[")+1,flavorValue.indexOf("]"));
            if(StringUtils.isNotEmpty(quflavorValue)) {
                String[] flavor_array = quflavorValue.split(",");
                cuisineFlavorVO.setFlavorData(Arrays.asList(flavor_array));
            }
            cuisineFlavorVOList.add(cuisineFlavorVO);
        }
        cuisineVO.setDishFlavors(cuisineFlavorVOList);
        return cuisineVO;
    }

    @ApiOperation(value = "查询可用的菜品列表")
    @GetMapping("/findAvailableDishList/{categoryId}")
    public List<Map<String,Object>> findAvailableCuisineList(@PathVariable String categoryId,
                                                       @RequestParam(value="name",required=false) String name){
        return cuisineService.findAvailableCuisineListInfo(categoryId, name);
    }


}
