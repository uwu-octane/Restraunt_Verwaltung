package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.SellCalculation;
import com.restkeeper.store.mapper.SellCalculationMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service("sellCalculationService")
@Service(version = "1.0.0",protocol = "dubbo")
public class SellCalculationServiceImpl extends ServiceImpl<SellCalculationMapper, SellCalculation> implements ISellCalculationService {
    @Override
    public Integer getRemainCuisineNum(String cuisineId) {
        QueryWrapper<SellCalculation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().select(SellCalculation::getRemainder).eq(SellCalculation::getDishId,cuisineId);
        SellCalculation sellCalculation = this.getOne(queryWrapper);

        if( sellCalculation == null) {
            //no num limit
            return -1;
        }

        return sellCalculation.getRemainder();
    }

    @Override
    @Transactional
    public void decrease(String dishId, Integer dishNumber) {
        QueryWrapper<SellCalculation> queryWrapper =new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId);

        SellCalculation sellCalculation = this.getOne(queryWrapper);
        if (sellCalculation != null){
            int resultNum = sellCalculation.getRemainder() - dishNumber;
            if (resultNum < 0) resultNum = 0;
            sellCalculation.setRemainder(resultNum);
            this.updateById(sellCalculation);
        }
    }

    @Override
    @Transactional
    public void add(String dishId, int dishNumber) {
        QueryWrapper<SellCalculation> queryWrapper =new QueryWrapper<>();
        queryWrapper.lambda().eq(SellCalculation::getDishId, dishId);

        SellCalculation sellCalculation = this.getOne(queryWrapper);
        if (sellCalculation != null){
            int resultNum = sellCalculation.getRemainder() + dishNumber;
            sellCalculation.setRemainder(resultNum);
            this.updateById(sellCalculation);
        }
    }
}
