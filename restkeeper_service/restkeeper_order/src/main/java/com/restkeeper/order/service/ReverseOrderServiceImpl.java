package com.restkeeper.order.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.order.mapper.ReverseOrderMapper;
import com.restkeeper.service.IOrderDetailService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReverseOrderService;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service(version = "1.0.0",protocol = "dubbo")
@org.springframework.stereotype.Service("reverseOrderService")
public class ReverseOrderServiceImpl extends ServiceImpl<ReverseOrderMapper, ReverseOrder> implements IReverseOrderService {

    @Autowired
    @Qualifier("orderService")
    private IOrderService orderService;

    @Override
    @Transactional
    public boolean reverse(ReverseOrder reverseOrder) {
        //update original order payment status
        String orderId = reverseOrder.getOrderId();
        OrderEntity orderEntity = orderService.getById(orderId);
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_UNPAIED);
        orderService.updateById(orderEntity);

        //add record
        reverseOrder.setCreateTime(LocalDateTime.now());
        reverseOrder.setOrderNumber(orderEntity.getOrderNumber());
        reverseOrder.setTableId(orderEntity.getTableId());
        reverseOrder.setAmount(orderEntity.getPayAmount());
        //add operator name
        String loginUserName = RpcContext.getContext().getAttachment("loginUserName");
        reverseOrder.setOperatorName(loginUserName);
        return this.save(reverseOrder);
    }
}
