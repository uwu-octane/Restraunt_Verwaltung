package com.restkeeper.utils;

import lombok.Data;

import java.io.Serializable;

/**
 * 返回结果通用封装
 */
@Data
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;

    // 返回状态
    private int status;
    // 状态描述
    private String desc;
    // 返回数据
    private Object data;

    private String token;
}