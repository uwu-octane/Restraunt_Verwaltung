package com.restkeeper.controller.shop;

import com.restkeeper.response.vo.PageVO;
import com.restkeeper.shop.entity.Brand;
import com.restkeeper.shop.service.IBrandService;
import com.restkeeper.vo.shop.AddTBrandVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@Api(tags = {"Brand Management Interface"})
@RestController
@RequestMapping("/brand")
public class brandController {

    @Reference(version = "1.0.0", check = false)
    private IBrandService brandService;

    @ApiOperation(value = "Paging for all brand information, enterprise user has to log in first")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "Current page number", required = false, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "page size", required = false, dataType = "Integer")})
    @GetMapping("/pageList/{page}/{pageSize}")
    public PageVO<Brand> pageList(@PathVariable("page") int page, @PathVariable("pageSize") int pageSize){
        return new PageVO<Brand>(brandService.paginationQuery(page,pageSize));
    }


    @ApiOperation(value = "add brand")
    @PostMapping("/add")
    public boolean add(@RequestBody AddTBrandVO addTBrandVO){
        Brand brand = new Brand();
        BeanUtils.copyProperties(addTBrandVO, brand);
        return brandService.save(brand);
    }


    @ApiOperation(value = "Brand List（下拉选择使用）")
    @GetMapping("/brandList")
    @ResponseBody
    public List<Map<String, Object>> listBrand(){
        return brandService.getBrandList();
    }

}
