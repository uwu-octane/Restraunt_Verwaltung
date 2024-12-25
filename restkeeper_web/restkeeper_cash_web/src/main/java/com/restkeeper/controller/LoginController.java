package com.restkeeper.controller;


import com.restkeeper.store.service.IStaffService;
import com.restkeeper.utils.Result;
import com.restkeeper.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"Cashier Login Interface"})
@RestController
public class LoginController {

    @Reference(version = "1.0.0", check=false)
    private IStaffService staffService;

    @ApiOperation(value = "登录校验")
    @ApiImplicitParam(name = "Authorization", value = "jwt token", required = false, dataType = "String",paramType="header")
    @PostMapping("/login")
    public Result loginStaff(@RequestBody LoginVO loginVO){
        return staffService.loginStaff(loginVO.getShopId(),loginVO.getLoginName(),loginVO.getLoginPass());
    }
}
