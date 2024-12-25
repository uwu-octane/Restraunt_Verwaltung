package com.restkeeper.operator.controller;

import com.restkeeper.operator.entity.EnterpriseAccount;
import com.restkeeper.operator.service.IEnterpriseAccountService;
import com.restkeeper.operator.vo.AddEnterpriseAccountVO;
import com.restkeeper.response.vo.ResetPwdVO;
import com.restkeeper.operator.vo.UpdateEnterpriseAccountVO;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.utils.AccountStatus;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Api(tags = {"Enterprise Account Management Interface"})
@RestController
@RequestMapping("enterprise")
public class EnterpriseAccountController {

    @Reference(version = "1.0.0", check = false)
    private IEnterpriseAccountService enterpriseAccountService;


    @ApiOperation("enterprise account query")
    @GetMapping("/pageList/{page}/{pageSize}")
    public PageVO<EnterpriseAccount> findListByPage(@PathVariable("page")int page, @PathVariable("pageSize") int pageSize,
                                                    @RequestParam(value = "enterpriseName", required = false) String enterpriseName ){
        return new PageVO<EnterpriseAccount>(enterpriseAccountService.queryPageByName(page,pageSize,enterpriseName));
    }

    @ApiOperation("add new enterpirse account")
    @PostMapping("/add")
    public boolean add(@RequestBody AddEnterpriseAccountVO addEnterpriseAccountVO){
        //bean copy
        EnterpriseAccount enterpriseAccount = new EnterpriseAccount();
        BeanUtils.copyProperties(addEnterpriseAccountVO, enterpriseAccount);

        //set time
        LocalDateTime localDateTime = LocalDateTime.now();

        //application time
        enterpriseAccount.setApplicationTime(localDateTime);

        //set expire time
        LocalDateTime expireTime = null;
        //default 7 days for on trial
        if (addEnterpriseAccountVO.getStatus() == 0) {
            expireTime = localDateTime.plusDays(7);
        }

        if(addEnterpriseAccountVO.getStatus() == 1) {
            expireTime = localDateTime.plusDays(addEnterpriseAccountVO.getValidityDay());
        }

        if (expireTime != null){
            enterpriseAccount.setExpireTime(expireTime);
        } else {
            throw new RuntimeException("account information error");
        }
        return enterpriseAccountService.add(enterpriseAccount);
    }

    //get account info with id
    @ApiOperation("enterpirse account query")
    @GetMapping("/getById/{id}")
    public EnterpriseAccount getById(@PathVariable("id") String id){
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById(id);
        if (enterpriseAccount == null) {
            throw new RuntimeException("not found");
        }
        return enterpriseAccount;
    }

    @ApiOperation("enterpirse account update")
    @PutMapping("/update")
    public Result update(@RequestBody UpdateEnterpriseAccountVO updateEnterpriseAccountVO){
        Result result = new Result();

        //get old info
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById((updateEnterpriseAccountVO.getEnterpriseId()));

        if (enterpriseAccount == null) {
            result.setStatus(ResultCode.error);
            result.setDesc("Enterprise account doesnt exist");
            return  result;
        }
        if (updateEnterpriseAccountVO.getStatus() != null) {
            int statusToUpdate = updateEnterpriseAccountVO.getStatus();
            int currentStatus = enterpriseAccount.getStatus();

            //official cant be set back to trial
            if (statusToUpdate == AccountStatus.Trial.getStatus() && currentStatus == AccountStatus.Official.getStatus()) {
                result.setStatus(ResultCode.error);
                result.setDesc("official account cant be set back to trial account");
                return result;
            }

            //trail account to official
            if(statusToUpdate == AccountStatus.Official.getStatus() && currentStatus == AccountStatus.Trial.getStatus()) {
                //expire time
                LocalDateTime localDateTime = LocalDateTime.now();
                LocalDateTime expireTime =  localDateTime.plusDays(updateEnterpriseAccountVO.getValidityDay());
                enterpriseAccount.setApplicationTime(localDateTime);
                enterpriseAccount.setExpireTime(expireTime);
            }

            //extend official account activation time
            if (statusToUpdate == AccountStatus.Official.getStatus() && currentStatus == AccountStatus.Official.getStatus()) {
                LocalDateTime localDateTime = LocalDateTime.now();
                LocalDateTime expireTime =  localDateTime.plusDays(updateEnterpriseAccountVO.getValidityDay());
                enterpriseAccount.setExpireTime(expireTime);
            }
        }

        BeanUtils.copyProperties(updateEnterpriseAccountVO,enterpriseAccount);

       boolean res =  enterpriseAccountService.updateById(enterpriseAccount);
       if (res) {
           result.setStatus(ResultCode.success);
           result.setDesc("Update succeed");
       } else {
           result.setStatus(ResultCode.error);
           result.setDesc("Update failed");
       }
        return result;
    }


    //logic del
    @ApiOperation("delete account")
    @DeleteMapping("/deleteById/{id}")
    public boolean deleteById(@PathVariable("id") String id) {
        if (enterpriseAccountService.isExist(id)) {
            return enterpriseAccountService.removeById(id);
        } else {
            throw new RuntimeException("user not found");
        }
    }

    @ApiOperation("recovert deleted enterprise account")
    @PutMapping("/recovery/{id}")
    public boolean recovery(@PathVariable("id") String id){
        if(enterpriseAccountService.isExist(id)){
            return enterpriseAccountService.recovery(id);
        } else {
            throw new RuntimeException("user not found");
        }
    }

    //ban account
    @ApiOperation("ban enterprise account")
    @PutMapping("/ban/{id}")
    public boolean ban(@PathVariable("id")String id){
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById(id);
        if (enterpriseAccount == null) {
            throw new RuntimeException("user not found");
        }
        enterpriseAccount.setStatus(AccountStatus.Forbidden.getStatus());
        return enterpriseAccountService.updateById(enterpriseAccount);
    }

    @ApiOperation("unblock enterprise account")
    @PutMapping("/unblock/{id}")
    public boolean unblock(@PathVariable("id")String id){
        EnterpriseAccount enterpriseAccount = enterpriseAccountService.getById(id);
        if (enterpriseAccount == null) {
            throw new RuntimeException("user not found");
        }
        LocalDateTime applicationTime = enterpriseAccount.getApplicationTime();
        LocalDateTime expireTime = enterpriseAccount.getExpireTime();
        long daysBetween = ChronoUnit.DAYS.between(applicationTime, expireTime);
        if (daysBetween > 7) {
            enterpriseAccount.setStatus(AccountStatus.Official.getStatus());
        } else {
            enterpriseAccount.setStatus(AccountStatus.Trial.getStatus());
        }
        return enterpriseAccountService.updateById(enterpriseAccount);
    }


    @ApiOperation("reset password")
    @PutMapping("/resetPassword")
    public boolean restPwd(@RequestBody ResetPwdVO resetPwdVO){
        return enterpriseAccountService.restPassword(resetPwdVO.getId(),resetPwdVO.getPwd());
    }

}

