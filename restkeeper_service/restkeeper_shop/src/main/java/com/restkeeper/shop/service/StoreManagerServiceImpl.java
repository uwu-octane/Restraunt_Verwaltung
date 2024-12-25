package com.restkeeper.shop.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.mapper.StoreManagerMapper;
import com.restkeeper.utils.JWTUtil;
import com.restkeeper.utils.MD5CryptUtil;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service(version = "1.0.0",protocol = "dubbo")
public class StoreManagerServiceImpl extends ServiceImpl<StoreManagerMapper, StoreManager> implements IStoreManagerService {

    @Value("${gateway.secret}")
    private String secret;




    @Autowired
    @Qualifier("storeService") //only in same module
    private IStoreService storeService;

    @Override
    public IPage<StoreManager> queryPageByCriteria(int pageNo, int pageSize, String criteria) {
        QueryWrapper<StoreManager> queryWrapper = new QueryWrapper<>();
        IPage<StoreManager> page = new Page<>(pageNo, pageSize);

        if (StringUtils.isNotEmpty(criteria)){
            queryWrapper.lambda().eq(StoreManager::getStoreManagerPhone, criteria).or().eq(StoreManager::getStoreManagerName,criteria);
        }
        return this.page(page,queryWrapper);
    }

    @Override
    @Transactional
    public boolean addStoreManager(String name, String email, String phone, List<String> storeIds) {
        boolean flag = true;

        try{
            StoreManager storeManager = new StoreManager();
            storeManager.setStoreManagerName(name);
            storeManager.setManagerEmail(email);
            storeManager.setStoreManagerPhone(phone);
            String pwd = RandomStringUtils.randomNumeric(8);
            storeManager.setPassword(Md5Crypt.md5Crypt(pwd.getBytes()));
            this.save(storeManager);

            String storeManagerId = storeManager.getStoreManagerId();
            UpdateWrapper<Store> updateWrapper = new UpdateWrapper<Store>();
            updateWrapper.lambda().in(Store::getStoreId, storeIds).set(Store::getStoreManagerId, storeManagerId);
            flag = storeService.update(updateWrapper);
            //todo: send email
        }catch (Exception e){
            flag = false;
            throw e;
        }

        return flag;
    }

    @Override
    @Transactional
    public boolean updateStoreManager(String storeManagerId, String newName, String newPhone, String newEmail, List<String> newStoreIds) {
        boolean flag = true;

        try {
            StoreManager storeManager = this.getById(storeManagerId);
            if (StringUtils.isNotEmpty(newName)){
                storeManager.setStoreManagerName(newName);
            }

            if (StringUtils.isNotEmpty(newEmail)){
                storeManager.setManagerEmail(newEmail);
            }

            if (StringUtils.isNotEmpty(newPhone)){
                storeManager.setStoreManagerPhone(newPhone);
            }

            this.updateById(storeManager);

            //remove old relationship between store and manager
            UpdateWrapper<Store> updateWrapper_old = new UpdateWrapper<>();
            updateWrapper_old.lambda().set(Store::getStoreManagerId, null).eq(Store::getStoreManagerId,storeManagerId);
            storeService.update(updateWrapper_old);
            //update new relationship
            UpdateWrapper<Store> updateWrapper_new = new UpdateWrapper<>();
            updateWrapper_new.lambda().in(Store::getStoreId, newStoreIds).set(Store::getStoreManagerId,storeManagerId);
            storeService.update(updateWrapper_new);
        } catch (Exception e){
            log.error("updata store manager failed " + e.getMessage());
            flag = false;
        }

        return flag;
    }

    @Override
    @Transactional
    public boolean logicDeleteStoreManager(String storeManagerId) {
        //logic delete
        this.removeById(storeManagerId);

        UpdateWrapper<Store> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(Store::getStoreManagerId, null).eq(Store::getStoreManagerId, storeManagerId);

        return storeService.update(updateWrapper);
    }

    @Override
    @Transactional
    public boolean suspendStoreManager(String storeManagerId) {
        UpdateWrapper<StoreManager> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(StoreManager::getStatus, SystemCode.FORBIDDEN).eq(StoreManager::getStoreManagerId,storeManagerId);
        return this.update(updateWrapper);
    }

    @Override
    public Result storeManagerLogin(String shopId, String email, String password) {
        Result result = new Result();

        //参数校验
        if (StringUtils.isEmpty(shopId)){
            result.setStatus(ResultCode.error);
            result.setDesc("shopId empty");
            return result;
        }
        if (StringUtils.isEmpty(email)){
            result.setStatus(ResultCode.error);
            result.setDesc("email empty");
            return result;
        }
        if (StringUtils.isEmpty(password)){
            result.setStatus(ResultCode.error);
            result.setDesc("password empty");
            return result;
        }
        QueryWrapper<StoreManager> storeManagerQueryWrapper = new QueryWrapper<>();
        storeManagerQueryWrapper.lambda().eq(StoreManager::getShopId, shopId).eq(StoreManager::getManagerEmail, email);
        RpcContext.getContext().setAttachment(SystemCode.TENANT_CONDITION_SHOPID, shopId);
        StoreManager storeManager = this.getOne(storeManagerQueryWrapper);

        if (storeManager == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("manager not exist");
            return result;
        }

        //get related store info
        List<Store> stores = storeManager.getStores();
        if (stores == null || stores.isEmpty()) {
            result.setStatus(ResultCode.error);
            result.setDesc("no related stores");
            return result;
        }
        Store store = stores.get(0);

        String salts = MD5CryptUtil.getSalts(storeManager.getPassword());
        if (!Md5Crypt.md5Crypt(password.getBytes(),salts).equals(storeManager.getPassword())){
            result.setStatus(ResultCode.error);
            result.setDesc("wrong password");
            return result;
        }
        Map<String,Object> tokenMap = Maps.newHashMap();
        tokenMap.put("shopId",shopId);
        tokenMap.put("storeId",store.getStoreId());
        tokenMap.put("loginUserId",storeManager.getStoreManagerId());
        tokenMap.put("loginUserName", storeManager.getStoreManagerName());
        tokenMap.put("userType",SystemCode.USER_TYPE_STORE_MANAGER); //门店管理员用户
        String tokenInfo = "";
        try {
            tokenInfo = JWTUtil.createJWTByObj(tokenMap,secret);
        } catch (IOException e) {
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("token generation failed");
            return result;
        }

        result.setStatus(ResultCode.success);
        result.setDesc("ok" + store.getStoreId());
        result.setData(storeManager);
        result.setToken(tokenInfo);
        return result;

    }

    @Override
    @Transactional
    public boolean resetPassword(String storeManagerId, String newPassword) {
        if (StringUtils.isNotEmpty(newPassword)){
            UpdateWrapper<StoreManager> storeManagerUpdateWrapper = new UpdateWrapper<>();
            storeManagerUpdateWrapper.lambda().set(StoreManager::getPassword, Md5Crypt.md5Crypt(newPassword.getBytes()))
                    .eq(StoreManager::getStoreManagerId,storeManagerId);
            //RpcContext.getContext().setAttachment("shopId", shopId);
           return this.update(storeManagerUpdateWrapper);
        }
        return false;
    }


}
