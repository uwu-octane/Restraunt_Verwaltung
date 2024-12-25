package com.restkeeper.shop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 品牌管理
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "t_brand",resultMap = "BaseResultMap")
@ApiModel(value="Brand Object", description="brand Management")
public class Brand extends BaseShopEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "brand id")
    @TableId(type = IdType.ASSIGN_ID)
    private String brandId;

    @ApiModelProperty(value = "brand name")
    private String brandName;

    @ApiModelProperty(value = "catering category")
    private String category;

    @ApiModelProperty(value = "logo pic path")
    private String logo;

    @ApiModelProperty(value = "contact person")
    private String contact;

    @ApiModelProperty(value = "contact person Phone number")
    private String contactPhone;

    @TableField(exist = false)
    private int storeCount; //门店总数

    @TableField(exist = false)
    private int cityCount; //城市总数

    @TableField(exist = false)
    private String info; //页面显示信息

    public String getInfo(){
        return "Current Brand has "+this.getStoreCount()+" stores located in "+ this.getCityCount() +" Cities ";
    }
}

