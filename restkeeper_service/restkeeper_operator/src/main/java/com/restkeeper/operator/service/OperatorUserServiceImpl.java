package com.restkeeper.operator.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.mysql.cj.util.StringUtils;
import com.restkeeper.operator.entity.OperatorUser;
import com.restkeeper.operator.mapper.OperatorUserMapper;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.MD5CryptUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.IOException;
import java.util.Map;

//@Service("operatorUserService")
@Service(version = "1.0.0",protocol = "dubbo")
/**
 * dubbo中支持的协议
 * dubbo 默认
 * rmi
 * hessian
 * http
 * webservice
 * thrift
 * memcached
 * redis
 */
@RefreshScope
public class OperatorUserServiceImpl extends ServiceImpl<OperatorUserMapper, OperatorUser> implements IOperatorUserService{

    @Value("${gateway.secret")
    private String secret;


    @Override
    public IPage<OperatorUser> queryPageByName(int pageNum, int pageSize, String name) {
        IPage<OperatorUser> page = new Page<>(pageNum,pageSize);

        //query condition constructor
        QueryWrapper<OperatorUser> queryWrapper = null;
        if (!StringUtils.isNullOrEmpty(name)){
            queryWrapper = new QueryWrapper<>();
            queryWrapper.like("loginname", name);
        }
        return this.page(page, queryWrapper);
    }

    @Override
    public Result login(String loginName, String loginPassWord) {
        Result result = new Result();

        //parameter validation
        if (StringUtils.isNullOrEmpty(loginName)){
            result.setStatus(ResultCode.error);
            result.setDesc("Username empty");
            return result;
        }

        if(StringUtils.isNullOrEmpty(loginPassWord)){
            result.setStatus(ResultCode.error);
            result.setDesc("Password empty");
            return result;
        }
        QueryWrapper<OperatorUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("loginname", loginName);
        OperatorUser operatorUser = this.getOne(queryWrapper);

        if (operatorUser == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("User not exist");
            return result;
        }

        String salts = MD5CryptUtil.getSalts(operatorUser.getLoginpass());
        if (!Md5Crypt.md5Crypt(loginPassWord.getBytes(), salts). equals(operatorUser.getLoginpass())) {
            result.setStatus(ResultCode.error);
            result.setDesc("Password or Username false");
            return result;
        }

        //jwt generation
        Map<String, Object> tokenInfo = Maps.newHashMap();
        tokenInfo.put("loginName", operatorUser.getLoginname());
        String token = null;

        try {
            token = JWTUtil.createJWTByObj(tokenInfo, secret);
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("token generate failed");
            return result;
        }

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(operatorUser);
        result.setToken(token);
        return result;

    }
}
