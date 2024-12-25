package com.restkeeper.controller.shop;


import com.restkeeper.response.vo.PageVO;
import com.restkeeper.response.vo.ResetPwdVO;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.service.IStoreManagerService;
import com.restkeeper.vo.shop.StoreManagerVO;
import io.swagger.annotations.Api;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = {"Store Manager Interface"})
@RequestMapping("/storeManager")
public class StoreManagerController {

    @Reference(version = "1.0.0", check = false)
    private IStoreManagerService storeManagerService;

    @ApiOperation(value = "查询分页数据")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "当前页码", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "分大小", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "name", value = "店长姓名", required = false, dataType = "String") })
    @PostMapping(value = "/pageList/{page}/{pageSize}")
    public PageVO<StoreManager> findListByPage(@PathVariable int page,
                                               @PathVariable int pageSize,
                                               @RequestParam String criteria) {
        return new PageVO<>(storeManagerService.queryPageByCriteria(page,pageSize,criteria));
    }


    @ApiOperation(value = "add new manager")
    @PostMapping("/add")
    public boolean addStoreManager(@RequestBody StoreManagerVO storeManagerVO){
        return storeManagerService.addStoreManager(
                storeManagerVO.getName(),storeManagerVO.getManagerEmail(),
                storeManagerVO.getPhone(),storeManagerVO.getStoreIds()
        );
    }

    @PutMapping("/updateManager")
    public boolean updateManager(@RequestBody StoreManagerVO storeManagerVO){
        return storeManagerService.updateStoreManager(storeManagerVO.getId(),storeManagerVO.getName(),storeManagerVO.getManagerEmail(),
                storeManagerVO.getPhone(),storeManagerVO.getStoreIds());

    }

    @ApiOperation(value = "删除数据")
    @DeleteMapping(value = "/del/{id}")
    public boolean delete(@PathVariable String id) {
        return storeManagerService.logicDeleteStoreManager(id);
    }

    @ApiOperation(value = "门店管理员停用")
    @PutMapping(value = "/pause/{id}")
    public boolean pause(@PathVariable String id) {
        return storeManagerService.suspendStoreManager(id);
    }

    @PutMapping(value = "/resetPassword")
    public boolean resetPassword(@RequestBody ResetPwdVO resetPwdVO){
        return storeManagerService.resetPassword(resetPwdVO.getId(),resetPwdVO.getPwd());
    }
}
