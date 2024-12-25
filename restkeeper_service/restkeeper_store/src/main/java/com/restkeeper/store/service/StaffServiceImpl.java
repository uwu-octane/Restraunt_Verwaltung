package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.mapper.StaffMapper;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.MD5CryptUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service(version = "1.0.0",protocol = "dubbo")
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements IStaffService {


    @Value("${gateway.secret}")
    private String secret;

    @Override
    @Transactional
    public boolean addStaff(Staff staff) {

        String pwd = staff.getPassword();
        if (StringUtils.isEmpty(pwd)){
            pwd = RandomStringUtils.randomNumeric(8);
        }
        staff.setPassword(Md5Crypt.md5Crypt(pwd.getBytes()));

        try {
            this.save(staff);
            //todo: send email
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }

        return true;
    }

    @Override
    public Result loginStaff(String shopId, String loginName, String loginPass) {
        Result result = new Result();
        QueryWrapper<Staff> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Staff::getStaffName, loginName).eq(Staff::getShopId,shopId);

        //Staff staff = this.getOne(queryWrapper);
        //call self defined login func
        Staff staff = this.getBaseMapper().login(shopId,loginName);
        if (staff == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("Staff not exist");
            return result;
        }

        String salts = MD5CryptUtil.getSalts(staff.getPassword());

        if(!Md5Crypt.md5Crypt(loginPass.getBytes(),salts).equals(staff.getPassword())){
            result.setStatus(ResultCode.error);
            result.setDesc("wrong password");
            return result;
        }

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put(SystemCode.TENANT_CONDITION_SHOPID, shopId);
        tokenMap.put(SystemCode.TENANT_CONDITION_STOREID, staff.getStoreId());
        tokenMap.put("loginUserId", staff.getStaffId());
        tokenMap.put("loginUserName", loginName);
        tokenMap.put("userType", SystemCode.USER_TYPE_STAFF);
        String tokenInfo = "";
        try{
            tokenInfo = JWTUtil.createJWTByObj(tokenMap,secret);
        }catch (Exception e){
            log.error("token generate failed called in staff login{}", e.getMessage());
            result.setStatus(ResultCode.error);
            result.setDesc("token generate failed called in staff login");
            return result;
        }

        result.setStatus(ResultCode.success);
        result.setDesc("ok");
        result.setData(staff);
        result.setToken(tokenInfo);
        return result;
    }
}
