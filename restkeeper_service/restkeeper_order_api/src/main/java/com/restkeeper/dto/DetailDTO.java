package com.restkeeper.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String detailId;
    private List<String> remarks;
}
