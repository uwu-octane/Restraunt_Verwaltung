package com.restkeeper.order.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.restkeeper.aop.TenantAnnotation;
import com.restkeeper.constants.OrderDetailType;
import com.restkeeper.constants.OrderPayType;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.dto.*;
import com.restkeeper.entity.OrderDetailEntity;
import com.restkeeper.entity.OrderDetailMealEntity;
import com.restkeeper.entity.OrderEntity;
import com.restkeeper.order.mapper.OrderMapper;
import com.restkeeper.service.IOrderDetailMealService;
import com.restkeeper.service.IOrderDetailService;
import com.restkeeper.service.IOrderService;
import com.restkeeper.store.entity.*;
import com.restkeeper.store.service.*;
import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.SequenceUtils;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@org.springframework.stereotype.Service("orderService")
@Service(version = "1.0.0",protocol = "dubbo")
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements IOrderService {

    @Autowired
    @Qualifier("orderDetailService")
    private IOrderDetailService orderDetailService;

    @Reference(version = "1.0.0", check = false)
    private ISellCalculationService sellCalculationService;

    @Reference(version = "1.0.0", check = false)
    private ITableService tableService;

    @Reference(version = "1.0.0", check = false)
    private ICreditService creditService;

    @Reference(version = "1.0.0", check = false)
    private ICreditCompanyUserService creditCompanyUserService;

    @Reference(version = "1.0.0", check = false)
    private ICreditLogService creditLogService;

    @Reference(version = "1.0.0", check = false)
    private ITableLogService tableLogService;

    @Reference(version = "1.0.0",check = false)
    private ISetMenuCuisineService setMenuCuisineService;

    @Autowired
    @Qualifier("orderDetailMealService")
    private IOrderDetailMealService orderDetailMealService;



    @Override
    @GlobalTransactional
    public String addOrder(OrderEntity orderEntity) {
        if(StringUtils.isEmpty(orderEntity.getOrderNumber())){
            String storeId = RpcContext.getContext().getAttachment("storeId");
            orderEntity.setOrderNumber(SequenceUtils.getSequence(storeId));
        }
        this.saveOrUpdate(orderEntity);

        List<OrderDetailEntity> orderDetailEntities = orderEntity.getOrderDetails();
        orderDetailEntities.forEach(orderDetailEntity -> {
            orderDetailEntity.setOrderId(orderEntity.getOrderId());
            orderDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(orderEntity.getOrderNumber()));

            TenantContext.addAttachment("shopId",RpcContext.getContext().getAttachment("shopId"));
            TenantContext.addAttachment("storeId",RpcContext.getContext().getAttachment("storeId"));

            Integer remainCuisineNum = sellCalculationService.getRemainCuisineNum(orderDetailEntity.getDishId());

            if(remainCuisineNum != -1){
                if (remainCuisineNum < orderDetailEntity.getDishNumber()) {
                    throw new RuntimeException(orderDetailEntity.getDishName() + " over remaining inventory");
                }
            }

            sellCalculationService.decrease(orderDetailEntity.getDishId(),orderDetailEntity.getDishNumber());
        });
        orderDetailService.saveBatch(orderDetailEntities);
        return orderEntity.getOrderId();
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean returnCuisine(DetailDTO cuisineToReturn) {

        OrderDetailEntity detailEntity = orderDetailService.getById(cuisineToReturn.getDetailId());
        Integer detailStatus = detailEntity.getDetailStatus();

        if (OrderDetailType.PLUS_DISH.getType() == detailStatus || OrderDetailType.NORMAL_DISH.getType() == detailStatus) {

            if (detailEntity.getDishNumber() <= 0) {
                throw new RuntimeException(detailEntity.getDishName() + " already be returned to 0");
            }

            OrderDetailEntity returnedDetailEntity = new OrderDetailEntity();
            BeanUtils.copyProperties(detailEntity,returnedDetailEntity);

            returnedDetailEntity.setDetailId(null);
            returnedDetailEntity.setStoreId(null);
            returnedDetailEntity.setShopId(null);

            returnedDetailEntity.setOrderNumber(SequenceUtils.getSequenceWithPrefix(detailEntity.getOrderNumber()));
            returnedDetailEntity.setDetailStatus(OrderDetailType.RETURN_DISH.getType());
            returnedDetailEntity.setDishNumber(1);
            returnedDetailEntity.setReturnRemark(cuisineToReturn.getRemarks().toString());
            orderDetailService.save(returnedDetailEntity);


        }

        //修改当前被退菜品在订单明细中的原有记录
        detailEntity.setDishNumber(detailEntity.getDishNumber()-1);
        detailEntity.setDishAmount(detailEntity.getDishNumber()*detailEntity.getDishPrice());
        orderDetailService.updateById(detailEntity);

        //修改订单主表信息
        OrderEntity orderEntity = this.getById(detailEntity.getOrderId());
        orderEntity.setTotalAmount(orderEntity.getTotalAmount()-detailEntity.getDishPrice());
        this.updateById(orderEntity);

        //判断沽清
        Integer remainderCount = sellCalculationService.getRemainCuisineNum(detailEntity.getDishId());
        if (remainderCount >0){
            //沽清中有该菜品
            //沽清数量+1
            sellCalculationService.add(detailEntity.getDishId(),1);

        }else{
            throw new RuntimeException("不支持退菜操作");
        }
        return true;
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean pay(OrderEntity orderEntity) {
        this.updateById(orderEntity);

        Table table = tableService.getById(orderEntity.getTableId());

        table.setStatus(SystemCode.TABLE_STATUS_FREE);
        tableService.updateById(table);

        return true;
    }

    @Override
    @Transactional
    @TenantAnnotation
    public boolean pay(OrderEntity orderEntity, CreditDTO creditDTO) {
        this.updateById(orderEntity);
        if (orderEntity.getPayType() == OrderPayType.CREDIT.getType()){
            String creditId = creditDTO.getCreditId();
            Credit credit = creditService.getById(creditId);

            if (credit.getCreditType() == SystemCode.CREDIT_TYPE_USER){
                if ( !credit.getUserName().equals(creditDTO.getCreditUserName())){
                    throw new RuntimeException("Person name not consist!");
                }

                credit.setCreditAmount(credit.getCreditAmount() + creditDTO.getCreditAmount());
                creditService.saveOrUpdate(credit);
            }

            List<CreditCompanyUser> companyUserList = null;
            if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
                List<CreditCompanyUser> companyUser =  creditCompanyUserService.getInfoList(creditId);
                Optional<CreditCompanyUser> resultInfo = companyUser.stream().filter(user -> user.getUserName().equals(creditDTO.getCreditUserName())).findFirst();
                if (!resultInfo.isPresent()){
                    throw new RuntimeException("current user is not in this company, contact management for help");
                }
                companyUserList = companyUser;
            }
            CreditLogs creditLogs = new CreditLogs();
            creditLogs.setCreditId(creditId);
            creditLogs.setOrderId(orderEntity.getOrderId());
            creditLogs.setType(credit.getCreditType());
           // creditLogs.setCreditAmount(creditDTO.getCreditAmount());
            creditLogs.setOrderAmount(orderEntity.getTotalAmount());
            creditLogs.setReceivedAmount(orderEntity.getTotalAmount());
            creditLogs.setCreditAmount(creditDTO.getCreditAmount());
            if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){

                creditLogs.setUserName(creditDTO.getCreditUserName());
                creditLogs.setCompanyName(credit.getCompanyName());
                Optional<CreditCompanyUser> optional = companyUserList.stream().filter(user -> user.getUserName().equals(creditDTO.getCreditUserName())).findFirst();
                String phone = optional.get().getPhone();
                creditLogs.setPhone(phone);

            }else if (credit.getCreditType() == SystemCode.CREDIT_TYPE_USER){
                creditLogs.setUserName(creditDTO.getCreditUserName());
                creditLogs.setPhone(credit.getPhone());
            }
            creditLogService.save(creditLogs);

            Table table = tableService.getById(orderEntity.getTableId());
            table.setStatus(SystemCode.TABLE_STATUS_FREE);
            tableService.updateById(table);
        }
        return false;
    }

    @Override
    @TenantAnnotation
    @Transactional
    public boolean changeTable(String orderId, String targetTableId) {
        Table targetTable= tableService.getById(targetTableId);
        if(targetTable==null){
            throw new RuntimeException("Table not exist");
        }

        if(targetTable.getStatus()!=SystemCode.TABLE_STATUS_FREE){
            throw new RuntimeException("This table is already occupied");
        }

        OrderEntity orderEntity = this.getById(orderId);
        Table sourceTable = tableService.getById(orderEntity.getTableId());
        //原来桌台设置为空闲
        sourceTable.setStatus(SystemCode.TABLE_STATUS_FREE);
        tableService.updateById(sourceTable);
        //目标桌台设置为开桌状态
        targetTable.setStatus(SystemCode.TABLE_STATUS_OPEND);
        tableService.updateById(targetTable);
        //新增开桌日志
        TableLog tableLog =new TableLog();
        tableLog.setTableStatus(SystemCode.TABLE_STATUS_OPEND);
        tableLog.setCreateTime(LocalDateTime.now());
        tableLog.setTableId(targetTableId);
        tableLog.setUserNumbers(orderEntity.getPersonNumbers());

        String loginUserName = RpcContext.getContext().getAttachment("loginUserName");

        tableLog.setUserId(loginUserName);
        tableLogService.save(tableLog);
        //修改订单桌台关系
        orderEntity.setTableId(targetTableId);
        return this.updateById(orderEntity);
    }

    @Override
    public CurrentAmountCollectDTO getCurrentCollect(LocalDate start, LocalDate end) {
        CurrentAmountCollectDTO result = new CurrentAmountCollectDTO();

        QueryWrapper<OrderEntity> queryWrapperPayment = new QueryWrapper<>();
        queryWrapperPayment.select(
                        "SUM(CASE WHEN pay_status = '" + SystemCode.ORDER_STATUS_PAIED + "' THEN pay_amount ELSE 0 END) AS total_paid_amount",
                        "SUM(CASE WHEN pay_status = '" + SystemCode.ORDER_STATUS_UNPAIED + "' THEN total_amount ELSE 0 END) AS total_unpaid_amount"
                )
                .lambda()
                .ge(OrderEntity::getLastUpdateTime, start)
                .lt(OrderEntity::getLastUpdateTime, end);

        // Execute the query and retrieve the results
        Map<String, Object> totals = this.getMap(queryWrapperPayment);

        // Set the unpaid total amount
        Integer unpaidTotal = totals.get("total_unpaid_amount") != null
                ? Integer.parseInt(totals.get("total_unpaid_amount").toString())
                : 0;
        result.setNoPayTotal(unpaidTotal);

        // Set the paid total amount
        Integer paidTotal = totals.get("total_paid_amount") != null
                ? Integer.parseInt(totals.get("total_paid_amount").toString())
                : 0;
        result.setPayTotal(paidTotal);

        // 合并查询，计算已付款和未付款订单总数
        QueryWrapper<OrderEntity> queryWrapperPaymentCount = new QueryWrapper<>();
        queryWrapperPaymentCount.select(
                        "SUM(CASE WHEN pay_status = '" + SystemCode.ORDER_STATUS_PAIED + "' THEN 1 ELSE 0 END) AS paid_order_count",
                        "SUM(CASE WHEN pay_status = '" + SystemCode.ORDER_STATUS_UNPAIED + "' THEN 1 ELSE 0 END) AS unpaid_order_count"
                )
                .lambda()
                .ge(OrderEntity::getLastUpdateTime, start)
                .lt(OrderEntity::getLastUpdateTime, end);

        // 执行查询并获取结果
        Map<String, Object> counts = this.getMap(queryWrapperPaymentCount);

        // 设置已付款订单总数
        Integer totalPayCount = counts.get("paid_order_count") != null
                ? Integer.parseInt(counts.get("paid_order_count").toString())
                : 0;
        result.setPayTotalCount(totalPayCount);

        // 设置未付款订单总数
        Integer noPayTotalCount = counts.get("unpaid_order_count") != null
                ? Integer.parseInt(counts.get("unpaid_order_count").toString())
                : 0;
        result.setNoPayTotalCount(noPayTotalCount);


        // 合并查询，计算已结账和未结账就餐人数
        QueryWrapper<OrderEntity> queryWrapperCustomerCount = new QueryWrapper<>();
        queryWrapperCustomerCount.select(
                        "SUM(CASE WHEN pay_status = '" + SystemCode.ORDER_STATUS_PAIED + "' THEN person_numbers ELSE 0 END) AS payed_person_numbers",
                        "SUM(CASE WHEN pay_status = '" + SystemCode.ORDER_STATUS_UNPAIED + "' THEN person_numbers ELSE 0 END) AS not_payed_person_numbers"
                )
                .lambda()
                .ge(OrderEntity::getLastUpdateTime, start)
                .lt(OrderEntity::getLastUpdateTime, end);

        // 执行查询并获取结果
        Map<String, Object> customer = this.getMap(queryWrapperCustomerCount);

        // 设置已结账就餐人数
        Integer payedTotalPerson = customer.get("payed_person_numbers") != null
                ? Integer.parseInt(customer.get("payed_person_numbers").toString())
                : 0;
        result.setTotalPerson(payedTotalPerson);

        // 设置未结账就餐人数
        Integer notPayTotalPerson = customer.get("not_payed_person_numbers") != null
                ? Integer.parseInt(customer.get("not_payed_person_numbers").toString())
                : 0;
        result.setCurrentPerson(notPayTotalPerson);
        
        return result;
    }

    @Override
    public List<CurrentHourCollectDTO> getCurrentHourCollect(LocalDate start, LocalDate end, Integer type) {

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();

        if (type == SystemCode.SUM_OF_TURNOVER){
            //针对销售额求和
            wrapper.select("sum(total_amount) as total_amount","hour(last_update_time) as current_date_hour")
                    .lambda().ge(OrderEntity::getLastUpdateTime,start)
                    .lt(OrderEntity::getLastUpdateTime,end);
        }

        if (type == SystemCode.SUM_OF_ORDER){
            //针对销售量（单数）求和
            wrapper.select("count(total_amount) as total_amount","hour(last_update_time) as current_date_hour")
                    .lambda().ge(OrderEntity::getLastUpdateTime,start)
                    .lt(OrderEntity::getLastUpdateTime,end);
        }

        //针对时间分组汇总
        wrapper.groupBy("current_date_hour").orderByAsc("current_date_hour");

        List<CurrentHourCollectDTO> result = Lists.newArrayList();

        this.getBaseMapper().selectList(wrapper).forEach(o->{

            CurrentHourCollectDTO item = new CurrentHourCollectDTO();

            item.setTotalAmount(o.getTotalAmount());
            item.setCurrentDateHour(o.getCurrentDateHour());

            result.add(item);
        });

        //当时间为null，设定值为0。
        for(int i=0;i<=23;i++){
            int hour = i;
            if (!result.stream().anyMatch(r->r.getCurrentDateHour() == hour)){
                CurrentHourCollectDTO item = new CurrentHourCollectDTO();
                item.setTotalAmount(0);
                item.setCurrentDateHour(hour);
                result.add(item);
            }
        }

        //对结果根据小时从小到大排序
        result.sort((a,b)->Integer.compare(a.getCurrentDateHour(),b.getCurrentDateHour()));

        return result;
    }

    @Override
    public List<PayTypeCollectDTO> getPayTypeCollect(LocalDate start, LocalDate end) {
        List<PayTypeCollectDTO> result = Lists.newArrayList();

        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();
        wrapper.select("pay_type","sum(pay_amount) as total_amount")
                .lambda().ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,SystemCode.ORDER_STATUS_PAIED)
                .groupBy(OrderEntity::getPayType);
        List<OrderEntity> orderEntityList = this.getBaseMapper().selectList(wrapper);

        orderEntityList.forEach(orderEntity->{

            PayTypeCollectDTO payTypeCollectDTO = new PayTypeCollectDTO();

            payTypeCollectDTO.setPayType(orderEntity.getPayType());
            payTypeCollectDTO.setPayName(PayType.getName(orderEntity.getPayType()));
            payTypeCollectDTO.setTotalCount(orderEntity.getTotalAmount());
            result.add(payTypeCollectDTO);
        });

        return result;
    }

    @Override
    public PrivilegeDTO getPrivilegeCollect(LocalDate start, LocalDate end) {
        QueryWrapper<OrderEntity> wrapper = new QueryWrapper<>();
        wrapper.select("sum(present_amount) as present_amount","sum(free_amount) as free_amount","sum(small_amount) as small_amount")
                .lambda()
                .ge(OrderEntity::getLastUpdateTime,start)
                .lt(OrderEntity::getLastUpdateTime,end)
                .eq(OrderEntity::getPayStatus,1);
        OrderEntity orderEntity = this.baseMapper.selectOne(wrapper);
        PrivilegeDTO result = new PrivilegeDTO();
        result.setFreeAmount(orderEntity.getFreeAmount());
        result.setPresentAmount(orderEntity.getPresentAmount());
        result.setSmallAmount(orderEntity.getSmallAmount());

        return result;
    }

    //统计计算向t_order_detail_meal中存放数据
    @TenantAnnotation
    private void saveOrderDetailMealInfo(String orderId){
        //获取订单明细表中的套餐信息
        QueryWrapper<OrderDetailEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(OrderDetailEntity::getOrderId,orderId).eq(OrderDetailEntity::getDishType,2);
        List<OrderDetailEntity> orderDetailSetMealList = orderDetailService.list(wrapper);

        //获取套餐下的菜品信息
        orderDetailSetMealList.forEach(orderSetMeal->{

            //通过套餐id获取该套餐下的菜品信息
            List<Cuisine> dishList = setMenuCuisineService.getAllCuisineBySetMenuId(orderSetMeal.getDishId());

            //插入到orderdetaimeal中（套餐中的每一个菜品信息）
            OrderDetailMealEntity orderDetailMealEntity = new OrderDetailMealEntity();
            //复制公有信息
            BeanUtils.copyProperties(orderSetMeal,orderDetailMealEntity);

            //当前套餐的优惠比率= 套餐支付金额/套餐中所有菜品的原价总和
            float allDishPriceInSetMeal = dishList.stream().map(d->d.getPrice()*setMenuCuisineService.getCuisineCopiesInSetMenu(d.getId(),orderSetMeal.getDishId())).reduce(Integer::sum).get()*orderSetMeal.getDishNumber();
            float rate = orderSetMeal.getDishAmount() / allDishPriceInSetMeal;

            //循环补充其他信息
            dishList.forEach(d->{
                orderDetailMealEntity.setDetailId(null);
                orderDetailMealEntity.setShopId(null);
                orderDetailMealEntity.setStoreId(null);
                orderDetailMealEntity.setDishId(d.getId());
                orderDetailMealEntity.setDishName(d.getName());
                orderDetailMealEntity.setDishPrice(d.getPrice());
                orderDetailMealEntity.setDishType(1);
                orderDetailMealEntity.setDishCategoryName(d.getCuisineCategory().getName());
                Integer dishCopies = setMenuCuisineService.getCuisineCopiesInSetMenu(d.getId(), orderSetMeal.getDishId());
                orderDetailMealEntity.setDishNumber(orderSetMeal.getDishNumber()*dishCopies);

                orderDetailMealEntity.setDishAmount((int)(d.getPrice()*dishCopies*orderSetMeal.getDishNumber()*rate));

                orderDetailMealService.save(orderDetailMealEntity);
            });
        });
    }
}
