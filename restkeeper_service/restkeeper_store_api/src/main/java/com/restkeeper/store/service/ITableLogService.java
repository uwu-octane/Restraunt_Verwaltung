package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;

public interface ITableLogService extends IService<TableLog> {
    boolean openTable(TableLog tableLog);

    TableLog getOpenTableLog(String tableId);
}
