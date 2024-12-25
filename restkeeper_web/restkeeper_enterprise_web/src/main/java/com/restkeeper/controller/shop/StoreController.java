package com.restkeeper.controller.shop;


import com.restkeeper.constants.SystemCode;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.service.IStoreService;
import com.restkeeper.utils.Result;
import com.restkeeper.vo.shop.AddStoreVO;
import com.restkeeper.vo.shop.StoreManagerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.runtime.StoredScript;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"Store Information Interface"})
@RestController
@Slf4j
@RequestMapping("/store")
public class StoreController {

    @Reference(version = "1.0.0", check=false)
    private IStoreService storeService;

    @ApiOperation(value = "query all store in page")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "current page", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "page size", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "query", name = "name", value = "store name", required = false, dataType = "String") })
    @GetMapping("/pageList/{page}/{pageSize}")
    public PageVO<Store> findStroeListByPage(@PathVariable int page, @PathVariable int pageSize,
                                             @RequestParam(value = "stroeName", required = false) String storeName){
        return new PageVO<Store>(storeService.paginationQueryByName(page,pageSize,storeName));
    }


    @ApiOperation(value = "add new store")
    @PostMapping(value = "/addStore")
    public boolean addStore(@RequestBody AddStoreVO addStoreVO){
        Store store = new Store();
        BeanUtils.copyProperties(addStoreVO,store);

        return storeService.save(store);
    }

    @ApiOperation(value = "disable store function")
    @ApiImplicitParam(paramType = "path", name = "storeId", value = "主键", required = true, dataType = "String")
    @PutMapping(value = "/disable/{storeId}")
    public boolean disableStore(@PathVariable String storeId){

        // Implicit Passing of Parameters Missing using dubbo in a serial of multi service call
        Store store = storeService.getById(storeId);
        store.setStatus(SystemCode.FORBIDDEN);

        return storeService.updateById(store);
    }

    @ApiOperation(value = "获取门店省份信息")
    @GetMapping("/listProvince")
    @ResponseBody
    public List<String> listProvince() {
        return storeService.getAllProvince();
    }

    @ApiOperation(value = "根据省份获取门店列表")
    @GetMapping("/getStoreByProvince/{province}")
    @ResponseBody
    public List<StoreDTO> getStoreByProvince(@PathVariable String province) {
        return storeService.getStoreByProvince(province);
    }


    @GetMapping("/getStoreByManagerId")
    @ResponseBody
    public List<StoreDTO> getStoreByManagerId() {
        return storeService.getStoreListByManagerId();
    }

    @ApiOperation(value = "门店切换")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "storeId", value = "门店Id", dataType = "String")})
    @GetMapping(value = "/switchStore/{storeId}")
    public Result switchStore(@PathVariable("storeId") String storeId){
        return storeService.switchStore(storeId);
    }

}
