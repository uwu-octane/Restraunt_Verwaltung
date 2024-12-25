package com.restkeeper.operator.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AddEnterpriseAccountVO {

    @ApiModelProperty(value = "enterprise name")
    private String enterpriseName;

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

    @ApiModelProperty(value = "status(On trial 0, deactivated -1, official 1)")
    private Integer status;

    @ApiModelProperty(value = "for official account，convert to days to backend")
    private int validityDay;

    @ApiModelProperty(value = "Enterprise email address")
    private String enterpriseEmailAddress;
}
