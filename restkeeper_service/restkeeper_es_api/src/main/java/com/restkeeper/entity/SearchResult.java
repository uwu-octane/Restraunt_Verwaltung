package com.restkeeper.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> records;
    private long total;
}

