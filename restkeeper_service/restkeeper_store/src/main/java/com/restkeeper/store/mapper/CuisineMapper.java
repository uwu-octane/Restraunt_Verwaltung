package com.restkeeper.store.mapper;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.redis.MybatisRedisCache;
import com.restkeeper.store.entity.Cuisine;
//import com.restkeeper.store.entity.Table;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
@CacheNamespace(implementation= MybatisRedisCache.class,eviction=MybatisRedisCache.class)
public interface CuisineMapper extends BaseMapper<Cuisine>{

    @Select("select * from t_dish where category_id=#{cuisineCategoryId} and is_deleted=0 order by last_update_time desc")
    List<Cuisine> selectCuisineByCategoryId(@Param("cuisineCategoryId") String cuisineCategoryId);
	
}
