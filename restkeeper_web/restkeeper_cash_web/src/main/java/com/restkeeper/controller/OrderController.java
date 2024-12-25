package com.restkeeper.controller;

import com.google.common.collect.Lists;
import com.restkeeper.constants.OrderPayType;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.dto.CreditDTO;
import com.restkeeper.dto.DetailDTO;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.entity.ReverseOrder;
import com.restkeeper.service.IOrderService;
import com.restkeeper.service.IReverseOrderService;
import com.restkeeper.store.entity.Cuisine;
import com.restkeeper.store.entity.CuisineCategory;
import com.restkeeper.store.service.ICuisineService;
import com.restkeeper.store.service.ISetMenuService;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import com.restkeeper.utils.SequenceUtils;
import com.restkeeper.vo.OrderDetailVO;
import com.restkeeper.vo.OrderVO;
import com.restkeeper.vo.PayVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Api(tags = {"Order Interface"})
@RestController
@RequestMapping("/order")
public class OrderController {

    @Reference(version = "1.0.0",check = false)
    private IOrderService orderService;

    @Reference(version = "1.0.0",check = false)
    private ICuisineService cuisineService;

    @Reference(version = "1.0.0",check = false)
    private ISetMenuService setMenuService;

    @Reference(version = "1.0.0",check = false)
    private IReverseOrderService reverseOrderService;

    @ApiOperation("order")
    @PostMapping("/add")
    public Result addOrder(@RequestBody OrderVO orderVO){

        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setTableId(orderVO.getTableId());
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_UNPAIED);
        orderEntity.setOperatorName(TenantContext.getLoginUserName());
        orderEntity.setOrderSource(SystemCode.ORDER_SOURCE_STORE);
        orderEntity.setTotalAmount(orderVO.getTotalAmount());
        orderEntity.setPersonNumbers(orderVO.getPersonNumbers());
        orderEntity.setOrderRemark(orderVO.getOrderRemark());
        orderEntity.setCreateTime(LocalDateTime.now());


        List<OrderDetailEntity> orderDetails = Lists.newArrayList();

        orderVO.getDishes().forEach(dishVO->{

            OrderDetailEntity orderDetailEntity = new OrderDetailEntity();

            orderDetailEntity.setDishName(dishVO.getDishName());
            orderDetailEntity.setDishType(dishVO.getType());
            orderDetailEntity.setDishId(dishVO.getDishId());
            orderDetailEntity.setDishAmount(dishVO.getDishNumber()*dishVO.getPrice());
            orderDetailEntity.setDishNumber(dishVO.getDishNumber());
            orderDetailEntity.setFlavorRemark(dishVO.getFlavorList().toString());
            orderDetailEntity.setTableId(orderVO.getTableId());
            orderDetailEntity.setDishPrice(dishVO.getPrice());
            CuisineCategory dishCategory = null;
            if (orderDetailEntity.getDishType() == 1){
                dishCategory = cuisineService.getById(dishVO.getDishId()).getCuisineCategory();
            }else{
                dishCategory = setMenuService.getById(dishVO.getDishId()).getCategory();
            }

            if (dishCategory!=null){
                orderDetailEntity.setDishCategoryName(dishCategory.getName());
            }
            orderDetails.add(orderDetailEntity);
        });
        orderEntity.setOrderDetails(orderDetails);


        String orderId = orderService.addOrder(orderEntity);

        Result result = new Result();
        result.setStatus(ResultCode.success);
        result.setDesc("new Order added");
        result.setData(orderId);
        return result;
    }


    @PostMapping("/plusDish/orderId/{orderId}")
    public Result orderPlusDish(@PathVariable String orderId, @RequestBody List<OrderDetailVO> details){

        OrderEntity orderEntity = orderService.getById(orderId);

        List<OrderDetailEntity> orderDetailEntities =new ArrayList<>();

        int amount = 0;

        for (OrderDetailVO detail : details) {
            OrderDetailEntity orderDetailEntity = new OrderDetailEntity();

            orderDetailEntity.setOrderId(orderEntity.getOrderId());
            orderDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(orderEntity.getOrderNumber()));
            orderDetailEntity.setTableId(orderEntity.getTableId());
            orderDetailEntity.setDetailStatus(detail.getStatus());
            orderDetailEntity.setDishId(detail.getDishId());
            orderDetailEntity.setDishType(detail.getType());
            orderDetailEntity.setDishName(detail.getDishName());
            orderDetailEntity.setDishPrice(detail.getPrice());
            orderDetailEntity.setDishNumber(detail.getDishNumber());
            orderDetailEntity.setDishAmount(detail.getDishNumber()*detail.getPrice());
            orderDetailEntity.setDishRemark(detail.getDishRemark());
            orderDetailEntity.setFlavorRemark(detail.getFlavorList().toString());

            Cuisine dish = cuisineService.getById(detail.getDishId());
            if(dish!=null){
                orderDetailEntity.setDishCategoryName(dish.getCuisineCategory().getName());
            }

            //当前加菜总金额
            amount+=orderDetailEntity.getDishAmount();

            orderDetailEntities.add(orderDetailEntity);
        }

        orderEntity.setOrderDetails(orderDetailEntities);
        orderEntity.setTotalAmount(orderEntity.getTotalAmount()+amount);

        Result result = new Result();
        result.setStatus(ResultCode.success);
        orderId = orderService.addOrder(orderEntity);
        result.setData(orderId);
        return result;
    }

    @PostMapping("/returnDish/{detailId}")
    public boolean returnDish(@PathVariable String detailId, @RequestBody List<String> remarks){
        DetailDTO detailDTO = new DetailDTO();
        detailDTO.setRemarks(remarks);
        detailDTO.setDetailId(detailId);
        return  orderService.returnCuisine(detailDTO);
    }

    @PostMapping("/pay/orderId/{orderId}")
    public boolean pay(@PathVariable String orderId, @RequestBody PayVO payVO){
        OrderEntity orderEntity= orderService.getById(orderId);
        orderEntity.setPayAmount(payVO.getPayAmount());
        orderEntity.setSmallAmount(payVO.getSmallAmount());
        orderEntity.setPayStatus(SystemCode.ORDER_STATUS_PAIED);
        orderEntity.setPayType(payVO.getPayType());

        if(payVO.getPayType() == OrderPayType.CREDIT.getType()){
            CreditDTO creditDTO =new CreditDTO();
            creditDTO.setCreditId(payVO.getCreditId());
            creditDTO.setCreditAmount(payVO.getCreditAmount());
            creditDTO.setCreditUserName(payVO.getCreditUserName());
            return orderService.pay(orderEntity,creditDTO);
        }
        return orderService.pay(orderEntity);
    }

    @PostMapping("/reverse/{orderId}")
    public boolean reverse(@PathVariable String orderId, @RequestBody  List<String> remarks){
        ReverseOrder reverseOrder=new ReverseOrder();
        reverseOrder.setOrderId(orderId);
        reverseOrder.setRemark(remarks.toString());
        return reverseOrderService.reverse(reverseOrder);
    }
}
