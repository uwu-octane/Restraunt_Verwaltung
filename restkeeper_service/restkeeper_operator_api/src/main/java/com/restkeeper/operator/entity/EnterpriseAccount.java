package com.restkeeper.operator.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 企业账号管理
 * </p>
 */
@Data
@Accessors(chain = true)
@TableName(value="t_enterprise_account")
@ApiModel(value="EnterpriseAccount Object", description="Enterprise Account Management Objects")
public class EnterpriseAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "enterprise id")
    @TableId(type= IdType.ASSIGN_ID)
    private String enterpriseId;

    @ApiModelProperty(value = "enterprise name")
    private String enterpriseName;

    @JsonIgnore
    @ApiModelProperty(value = "password（auto distributed in the background）")
    private String password;

    @JsonIgnore
    @ApiModelProperty(value = "Merchant number（下发生成）") //商户号
    private String shopId;

    @ApiModelProperty(value = "applicant")
    private String applicant;

    @ApiModelProperty(value = "phone number")
    private String phone;

    @ApiModelProperty(value = "province")
    private String province;

    @ApiModelProperty(value = "city")
    private String city;

    @ApiModelProperty(value = "area")
    private String area;

    @ApiModelProperty(value = "detailed address")
    private String address;

    @ApiModelProperty(value = "application time（current time in minute）")
    private LocalDateTime applicationTime;

    @ApiModelProperty(value = "expireTime (Under the trial is the default expiration after seven days, the status is changed to deactivated)")
    private LocalDateTime expireTime;

    @ApiModelProperty(value = "status(On trial 0, deactivated -1, official 1)")
    private Integer status;

    @ApiModelProperty(value = "lastUpdateTime")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdateTime;

    @ApiModelProperty(value = "deleted 1, not deleted 0")
    @TableLogic(value = "0", delval = "1")
    private Integer isDeleted;

    @ApiModelProperty(value = "Enterprise email address")
    private String enterpriseEmailAddress;
}
