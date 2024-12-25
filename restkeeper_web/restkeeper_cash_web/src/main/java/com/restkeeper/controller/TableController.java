package com.restkeeper.controller;


import com.azure.core.annotation.Get;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.Table;
import com.restkeeper.store.entity.TableLog;
import com.restkeeper.store.service.ITableAreaService;
import com.restkeeper.store.service.ITableLogService;
import com.restkeeper.store.service.ITableService;
import com.restkeeper.vo.TablePanelVO;
import com.restkeeper.vo.TableVO;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/table")
@Api(tags = {"Cash Site Table Area Interface"})
public class TableController {
    @Reference(version = "1.0.0", check=false)
    private ITableAreaService tableAreaService;

    @Reference(version = "1.0.0", check=false)
    private ITableLogService tableLogService;

    @Reference(version = "1.0.0", check=false)
    private ITableService tableService;

    @ApiOperation(value =  "list table area")
    @GetMapping("/listTableArea")
    public List<Map<String, Object>> listTableArea(){
        return tableAreaService.listTableArea();
    }


    @ApiOperation(value = "开桌")
    @PutMapping("/openTable/{tableId}/{numbers}")
    public boolean openTable(@PathVariable String tableId, @PathVariable Integer numbers){
        TableLog tableLog =new TableLog();
        tableLog.setTableId(tableId);
        tableLog.setUserNumbers(numbers);
        return tableLogService.openTable(tableLog);
    }


    @ApiOperation(value = "桌台面板")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "areaId", value = "区域Id", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "path", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "每页数量", required = true, dataType = "Integer")})
    @GetMapping("/queryByArea/{areaId}/{page}/{pageSize}")
    public TablePanelVO queryByArea(@PathVariable String areaId, @PathVariable int page, @PathVariable int pageSize) {
        TablePanelVO tablePanelVO = new TablePanelVO();
        //桌台统计信息
        tablePanelVO.setFreeNumbers(tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_FREE));

        tablePanelVO.setLockedNumbers(tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_LOCKED));
        tablePanelVO.setOpenedNumbers(tableService.countTableByStatus(areaId, SystemCode.TABLE_STATUS_OPEND));

        //桌台面板详情，支持分页
        IPage<Table> pageInfo = tableService.queryPageByAreaId(areaId, page, pageSize);
        List<TableVO> tableVOList = Lists.newArrayList();
        pageInfo.getRecords().forEach(d -> {
            TableVO tableVO = new TableVO();
            tableVO.setTableId(d.getTableId());
            tableVO.setTableName(d.getTableName());
            if (d.getStatus() == SystemCode.TABLE_STATUS_OPEND) {
                //从tableLog中获取就餐人数和时间
                TableLog tableLog = tableLogService.getOpenTableLog(d.getTableId());
                tableVO.setCreateTime(tableLog.getCreateTime());
                tableVO.setUserNumbers(tableLog.getUserNumbers());
            }
            tableVOList.add(tableVO);

        });

        PageVO<TableVO> pageVO = new PageVO<>(pageInfo,tableVOList);
        tablePanelVO.setTablePage(pageVO);

        return tablePanelVO;
    }

}
