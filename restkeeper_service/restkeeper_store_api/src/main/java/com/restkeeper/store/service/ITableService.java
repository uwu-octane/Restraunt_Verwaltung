package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Table;
import io.swagger.models.auth.In;

public interface ITableService extends IService<Table> {

    boolean addTable(Table table);


    IPage<Table> queryPageByAreaId(String areaId, int pageNum, int pageSize);

    Integer countTableByStatus(String areaId, Integer status);
}
