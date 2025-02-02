package com.restkeeper.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.store.entity.CuisineFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CuisineFlavorMapper extends BaseMapper<CuisineFlavor>{

  @Select("select * from t_dish_flavor where dish_id=#{cuisineId} order by last_update_time desc")
  List<CuisineFlavor> selectCuisineFlavor(@Param("cuisineId") String cuisineId);
}
