package com.restkeeper.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.dto.*;
import com.restkeeper.entity.OrderEntity;

import java.time.LocalDate;
import java.util.List;

public interface IOrderService extends IService<OrderEntity> {

    String addOrder(OrderEntity orderEntity);

    boolean returnCuisine(DetailDTO cuisineToReturn);

    //pay with cash
    boolean pay(OrderEntity orderEntity);

    //pay by on credit
    boolean pay(OrderEntity orderEntity, CreditDTO creditDTO);

    boolean changeTable(String orderId, String targetTableId);

    //get current turnover
    CurrentAmountCollectDTO getCurrentCollect(LocalDate start, LocalDate end);

    List<CurrentHourCollectDTO> getCurrentHourCollect(LocalDate start, LocalDate end, Integer type);


    List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start, LocalDate end);


    PrivilegeDTO getPrivilegeCollect(LocalDate start,LocalDate end);
}
