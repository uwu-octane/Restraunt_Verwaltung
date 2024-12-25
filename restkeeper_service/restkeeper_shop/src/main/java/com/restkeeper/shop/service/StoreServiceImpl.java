package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.mapper.StoreMapper;
import com.restkeeper.utils.BeanListUtils;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service("storeService")
@Service(version = "1.0.0",protocol = "dubbo")
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements IStoreService {
    @Override
    public IPage<Store> paginationQueryByName(int pageNo, int pageSize, String storeName) {
        IPage<Store> page = new Page<>(pageNo, pageSize);
        QueryWrapper <Store> queryWrapper = new QueryWrapper<>();

        if(StringUtils.isNotEmpty(storeName)){
            queryWrapper.lambda().like(Store::getStoreName, storeName);
        }
        return this.page(page,queryWrapper);
    }

    @Override
    public List<String> getAllProvince() {
        return getBaseMapper().getAllProvince();
    }

    @Override
    public List<StoreDTO> getStoreByProvince(String province) {
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Store::getStatus, 1);
        if (StringUtils.isNotEmpty(province) && !"all".equals(province)){
            queryWrapper.lambda().eq(Store::getProvince,province);
        }

        List<Store> list = this.list(queryWrapper);
        List<StoreDTO> list_dto;
        try{
            list_dto = BeanListUtils.copy(list, StoreDTO.class);
            return list_dto;
        } catch ( Exception e){
            e.printStackTrace();
            log.error("get store by province failed, caused by beanlist copy");
        }


        return new ArrayList<StoreDTO>();
    }

    @Override
    public List<StoreDTO> getStoreListByManagerId() {
        QueryWrapper<Store> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Store::getStatus, 1)
                .eq(Store::getStoreManagerId, RpcContext.getContext().getAttachment("loginUserId"));
        List<Store> list = this.list(queryWrapper);

        List<StoreDTO> list_dto;
        try{
            list_dto = BeanListUtils.copy(list, StoreDTO.class);
            return list_dto;
        } catch ( Exception e){
            e.printStackTrace();
            log.error("get store by manager id failed, caused by beanlist copy");
        }
        return new ArrayList<StoreDTO>();
    }


    @Value("${gateway.secret}")
    private String secret;

    @Override
    public Result switchStore(String storeIdToSwitch) {
        Result result = new Result();

        Map<String, Object> tokenMap = Maps.newHashMap();
        tokenMap.put("shopId", RpcContext.getContext().getAttachment("shopId"));
        tokenMap.put("storeId", storeIdToSwitch);
        tokenMap.put("loginUserId",RpcContext.getContext().getAttachment("loginUserId"));
        tokenMap.put("loginUserName",RpcContext.getContext().getAttachment("loginUserName"));
        tokenMap.put("UserType", SystemCode.USER_TYPE_STORE_MANAGER);

        String tokenInfo = "";
        try{
            tokenInfo = JWTUtil.createJWTByObj(tokenMap, secret);
        } catch (Exception e){
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("token generation failed");
            return result;
        }
        result.setData(storeIdToSwitch);
        result.setToken(tokenInfo);
        result.setStatus(ResultCode.success);
        result.setDesc("ok");

        return result;
    }


}
