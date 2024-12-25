package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.mapper.TableLogMapper;
import io.swagger.annotations.Api;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDateTime;

@org.springframework.stereotype.Service("tableLogService")
@Service(version = "1.0.0",protocol = "dubbo")
public class TableLogServiceImpl extends ServiceImpl<TableLogMapper, TableLog> implements ITableLogService {

    @Autowired
    @Qualifier("tableService")
    private ITableService tableService;


    @Override
    public boolean openTable(TableLog tableLog) {
        Table table = tableService.getById(tableLog.getLogId());
        if (SystemCode.TABLE_STATUS_FREE != table.getStatus()){
            throw new RuntimeException("This table is already occupied");
        }

        if (tableLog.getUserNumbers() > table.getTableSeatNumber()) {
            throw  new RuntimeException("Exceeded table limit and could not be opened");
        }

        table.setStatus(SystemCode.TABLE_STATUS_OPEND);
        tableService.updateById(table);

        tableLog.setUserId(RpcContext.getContext().getAttachment("loginUserName"));
        tableLog.setCreateTime(LocalDateTime.now());
        tableLog.setTableStatus(SystemCode.TABLE_STATUS_LOCKED);
        return this.save(tableLog);
    }

    @Override
    public TableLog getOpenTableLog(String tableId) {
        QueryWrapper<TableLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TableLog::getLogId,tableId).orderByDesc(TableLog::getCreateTime);

        return this.list(queryWrapper).get(0);
    }
}
