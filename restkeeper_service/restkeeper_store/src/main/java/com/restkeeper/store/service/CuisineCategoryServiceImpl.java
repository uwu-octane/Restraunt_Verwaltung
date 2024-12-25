package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.CuisineCategory;
import com.restkeeper.store.mapper.CuisineCategoryMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service(version = "1.0.0", protocol = "dubbo")
@org.springframework.stereotype.Service("CuisineCategoryService")
public class CuisineCategoryServiceImpl extends ServiceImpl<CuisineCategoryMapper, CuisineCategory> implements ICuisineCategoryService {

    @Override
    @Transactional
    public boolean add(String name, int type) {

        //check if category name already exist
        this.checkCategoryNameExist(name);

        CuisineCategory cuisineCategory = new CuisineCategory();
        cuisineCategory.setType(type);
        cuisineCategory.setName(name);
        cuisineCategory.setTorder(0);

        return this.save(cuisineCategory);
    }

    @Override
    @Transactional
    public boolean updateCategoryName(String id, String newCategoryName) {
        this.checkCategoryNameExist(newCategoryName);
        UpdateWrapper<CuisineCategory> updateWrapper = new UpdateWrapper<>();

        updateWrapper.lambda().set(CuisineCategory::getName,newCategoryName).eq(CuisineCategory::getCategoryId, id);

        return this.update(updateWrapper);
    }

    @Override
    public IPage<CuisineCategory> queryCategoryPage(int pageNum, int pageSize) {

        QueryWrapper<CuisineCategory> queryWrapper = new QueryWrapper<>();
        IPage<CuisineCategory> page = new Page<>(pageNum,pageSize);

        queryWrapper.lambda().orderByDesc(CuisineCategory::getLastUpdateTime);

        return this.page(page,queryWrapper);
    }

    @Override
    public List<Map<String, Object>> listCategory(Integer type) {
        QueryWrapper<CuisineCategory> queryWrapper = new QueryWrapper<>();
        if (type != null) {
            queryWrapper.lambda().eq(CuisineCategory::getType,type);
        }
        queryWrapper.lambda().select(CuisineCategory::getCategoryId, CuisineCategory::getName);


        return this.listMaps(queryWrapper);
    }


    private void checkCategoryNameExist(String name) {

        QueryWrapper<CuisineCategory> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(CuisineCategory::getCategoryId).eq(CuisineCategory::getName,name);

        Integer count = this.getBaseMapper().selectCount(queryWrapper);
        if(count> 0) throw new RuntimeException("this category already exist");
    }
}
