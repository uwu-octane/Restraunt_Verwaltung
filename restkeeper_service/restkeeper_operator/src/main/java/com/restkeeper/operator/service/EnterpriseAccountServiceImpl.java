package com.restkeeper.operator.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.google.common.collect.Maps;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.email.EmailObject;
import com.restkeeper.operator.config.RabbitMQConfig;
import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.mapper.EnterpriseAccountMapper;
import com.restkeeper.utils.*;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;


@Service(version = "1.0.0", protocol = "dubbo")
public class EnterpriseAccountServiceImpl extends ServiceImpl<EnterpriseAccountMapper, EnterpriseAccount> implements IEnterpriseAccountService{

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${gateway.secret}")
    private String secret;


    private void sendEmail(String recipientEmail, String shopId, String pwd, String subject, MessageType messageType) {
        EmailObject emailObject = new EmailObject();
        emailObject.setRecipientEmail(recipientEmail);
        emailObject.setSubject(subject);
        emailObject.setMessageType(messageType);
        emailObject.setPassword(pwd);
        emailObject.setShopId(shopId);
       /* JSONObject jsonObject = new JSONObject();
        jsonObject.put("shopId", shopId);
        jsonObject.put("password", pwd);
        emailObject.setMessageBody(jsonObject.toJSONString());*/

        rabbitTemplate.convertAndSend(RabbitMQConfig.Email_EXCHANGE, RabbitMQConfig.ACCOUNT_QUEUE_KEY, JSON.toJSONString(emailObject));
    }

    //paging query by name
    @Override
    public IPage<EnterpriseAccount> queryPageByName(int pageNum, int pageSize, String enterpriseName) {
        IPage<EnterpriseAccount> page = new Page<>(pageNum,pageSize);
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();

        if (StringUtils.isNotEmpty(enterpriseName)){
            queryWrapper.like("enterprise_name", enterpriseName);
        }
        return this.page(page,queryWrapper);
    }


    //add new account
    @Override
    @Transactional
    public boolean add(EnterpriseAccount enterpriseAccount) {
        boolean flag = true;

        try{
            //account password handel
            String shopId = getShopId();
            enterpriseAccount.setShopId(shopId);

            //password generation
            String pwd =  RandomStringUtils.randomNumeric(6);
            enterpriseAccount.setPassword(Md5Crypt.md5Crypt(pwd.getBytes()));
            this.save(enterpriseAccount);
            String subject = "Account Creation Succeed";
            sendEmail(enterpriseAccount.getEnterpriseEmailAddress(), enterpriseAccount.getEnterpriseId(),
                    pwd,subject, MessageType.ENTERPRISE_ACCOUNT_CREATION);
        }catch (Exception e){
            flag = false;
            throw e;
        }

        return flag;
    }


    //recovery del account
    @Override
    @Transactional
    public boolean recovery(String id) {
        return this.getBaseMapper().recovery(id);
    }

    @Override
    public boolean isExist(String id){
        EnterpriseAccount enterpriseAccount = this.getBaseMapper().selectByIdWithDeleted(id);
        return enterpriseAccount != null;
    }

    //reset password
    @Override
    @Transactional
    public boolean restPassword(String id, String password) {
        boolean flag = true;
        try{
            if (!this.isExist(id)){
                throw new RuntimeException("user not found");
            }
            EnterpriseAccount enterpriseAccount = this.getById(id);
            String newPassword;
            if (StringUtils.isNotEmpty(password)){
                newPassword = password;
            }else {
                newPassword = RandomStringUtils.randomNumeric(6);
            }
            enterpriseAccount.setPassword(Md5Crypt.md5Crypt(newPassword.getBytes()));
            this.updateById(enterpriseAccount);
            String subject = "You have reset your password";
            /*sendEmail(enterpriseAccount.getEnterpriseEmailAddress(), enterpriseAccount.getEnterpriseId(),
                    newPassword,subject,MessageType.ENTERPRISE_ACCOUNT_PASSWORD_RESET);*/
        }catch (Exception e){
            e.printStackTrace();
            flag = false;
            throw e;
        }
        return flag;
    }

    @Override
    public Result login(String loginShopId, String loginEmail, String loginPass) {
        Result result = new Result();

        if (StringUtils.isEmpty(loginShopId)){
            result.setStatus(ResultCode.error);
            result.setDesc("Shop id is empty");
            return result;
        }

        if (StringUtils.isEmpty(loginEmail)){
            result.setStatus(ResultCode.error);
            result.setDesc("Email address is empty");
            return result;
        }

        if (StringUtils.isEmpty(loginPass)){
            result.setStatus(ResultCode.error);
            result.setDesc("Password is empty");
            return result;
        }
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(EnterpriseAccount::getEnterpriseEmailAddress,loginEmail)
                .eq(EnterpriseAccount::getShopId,loginShopId);

        //not banned
        queryWrapper.lambda().notIn(EnterpriseAccount::getStatus, AccountStatus.Forbidden.getStatus());
        EnterpriseAccount enterpriseAccount = this.getOne(queryWrapper);

        if(enterpriseAccount == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("Account not exist");
            return result;
        }

        //password valid
        String salts = MD5CryptUtil.getSalts(enterpriseAccount.getPassword());
        if( !Md5Crypt.md5Crypt(loginPass.getBytes(),salts).equals(enterpriseAccount.getPassword())){
            result.setStatus(ResultCode.error);
            result.setDesc("Password Wrong");
            return result;
        }

        //token
        Map<String, Object> tokenInfo = Maps.newHashMap();
        tokenInfo.put("shopId", enterpriseAccount.getShopId());
        tokenInfo.put("loginEnterpriseName", enterpriseAccount.getEnterpriseName());
        tokenInfo.put("loginType", SystemCode.USER_TYPE_SHOP);

        String token = null;
        try {
            token = JWTUtil.createJWTByObj(tokenInfo,secret);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("token failed " +e.getMessage());
            result.setStatus(ResultCode.error);
            result.setDesc("token generation failed");
            return result;
        }

        result.setStatus(ResultCode.success);
        result.setDesc("login success");
        result.setData(enterpriseAccount);
        result.setToken(token);

        return result;
    }

    //generate shop id
    private String getShopId() {
        //random number
        String shopId = RandomStringUtils.randomNumeric(8);

        //validation
        QueryWrapper<EnterpriseAccount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("shop_id", shopId);

        EnterpriseAccount enterpriseAccount = this.getOne(queryWrapper);

        if (enterpriseAccount != null){
            this.getShopId();
        }
        return shopId;
    }


}
