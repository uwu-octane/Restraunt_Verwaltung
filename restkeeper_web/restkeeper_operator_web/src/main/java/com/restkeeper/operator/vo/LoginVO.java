package com.restkeeper.operator.vo;


import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

@Data
public class LoginVO {
    @ApiModelProperty(value = "login account")
    private String loginName;

    @ApiModelProperty(value = "pass word")
    private String loginPassWord;
}
