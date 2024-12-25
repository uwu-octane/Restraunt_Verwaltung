package com.restkeeper.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.redis.MybatisRedisCache;
import com.restkeeper.shop.entity.Brand;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Mapper;


//注解位置：@CacheNamespace 通常用于 MyBatis 的 Mapper 接口上，目的是为该 Mapper 接口提供一个共享的缓存空间（即二级缓存）。
//implementation：指定缓存的实现类。 eviction：指定缓存的清除策略实现类。
@Mapper
@CacheNamespace(implementation = MybatisRedisCache.class, eviction = MybatisRedisCache.class)
public interface BrandMapper extends BaseMapper<Brand> {

}
