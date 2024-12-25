package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.restkeeper.store.entity.SetMenuCuisine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetMenuCuisineMapper extends BaseMapper<SetMenuCuisine>{
    @Select(value="select * from t_setmeal_dish where setmeal_id=#{setMealId}")
    List<SetMenuCuisine> selectDishes(@Param("setMealId") String setMealId);
}
