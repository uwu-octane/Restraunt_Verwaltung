package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.mapper.BrandMapper;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;
import java.util.Map;

@Service(version = "1.0.0",protocol = "dubbo")
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements IBrandService {

    //desc pagination query of brand
    @Override
    public IPage<Brand> paginationQuery(int pageNo, int pageSize) {
        IPage<Brand> page = new Page<>(pageNo, pageSize);
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(Brand::getLastUpdateTime);
        return this.page(page,queryWrapper);
    }

    @Override
    public List<Map<String, Object>> getBrandList() {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();

        //get brand id and brand name
        queryWrapper.lambda().select(Brand::getBrandId,Brand::getBrandName);

        return this.listMaps(queryWrapper);
    }
}
