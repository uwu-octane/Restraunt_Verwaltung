package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.OrderDetailAllView;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.order.mapper.OrderDetailAllMapper;
import com.restkeeper.order.mapper.OrderDetailMapper;
import com.restkeeper.service.IOrderDetailService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Service("orderDetailService")
@Service(version = "1.0.0",protocol = "dubbo")
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetailEntity> implements IOrderDetailService {
    @Autowired
    private OrderDetailAllMapper orderDetailAllMapper;

    @Override
    public List<OrderDetailAllView> getCurrentCategoryAmountCollect(LocalDate start, LocalDate end) {
        QueryWrapper<OrderDetailAllView> wrapper = new QueryWrapper<>();
        wrapper.select("dish_category_name","sum(dish_amount) as dish_amount")
                .lambda()
                .ge(OrderDetailAllView::getLastUpdateTime,start)
                .lt(OrderDetailAllView::getLastUpdateTime,end)
                .eq(OrderDetailAllView::getDishType, SystemCode.DISH_TYPE_NORMAL)
                .groupBy(OrderDetailAllView::getDishCategoryName);

        return orderDetailAllMapper.selectList(wrapper);
    }

    @Override
    public List<OrderDetailAllView> getCurrentCategoryCountCollect(LocalDate start, LocalDate end) {
        QueryWrapper<OrderDetailAllView> wrapper = new QueryWrapper<>();
        wrapper.select("dish_category_name","count(dish_category_name) as total_count")
                .lambda()
                .ge(OrderDetailAllView::getLastUpdateTime,start)
                .lt(OrderDetailAllView::getLastUpdateTime,end)
                .eq(OrderDetailAllView::getDishType,SystemCode.DISH_TYPE_NORMAL)
                .groupBy(OrderDetailAllView::getDishCategoryName);

        return orderDetailAllMapper.selectList(wrapper);
    }

    @Override
    public List<OrderDetailAllView> getCurrentDishRank(LocalDate start, LocalDate end) {
        QueryWrapper<OrderDetailAllView> wrapper = new QueryWrapper<>();
        wrapper.select("dish_name","sum(dish_number) as total_count")
                .lambda().ge(OrderDetailAllView::getLastUpdateTime,start)
                .lt(OrderDetailAllView::getLastUpdateTime,end)
                .eq(OrderDetailAllView::getDishType,1)
                .groupBy(OrderDetailAllView::getDishName);
        wrapper.orderByDesc("total_count");
        return orderDetailAllMapper.selectList(wrapper);
    }
}
