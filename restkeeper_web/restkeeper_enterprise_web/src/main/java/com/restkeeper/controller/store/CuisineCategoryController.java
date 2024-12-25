package com.restkeeper.controller.store;

import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.CuisineCategory;
import com.restkeeper.store.service.ICuisineCategoryService;
import com.restkeeper.vo.store.AddDishCategoryVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Api(tags = { "Cuisine Category Management" })
@RestController
@RequestMapping("/cuisineCategory")
public class CuisineCategoryController {

    @Reference(version = "1.0.0",check = false)
    private ICuisineCategoryService cuisineCategoryService;


    @ApiOperation(value = "添加分类")
    @PostMapping("/addCategory")
    public boolean addCategory(@RequestBody AddDishCategoryVO addDishCategoryVO){
        return cuisineCategoryService.add(addDishCategoryVO.getCategoryName(),addDishCategoryVO.getType());
    }

    @ApiOperation(value = "修改分类")
    @PutMapping("/updateCategoryName/{id}")
    public boolean update(@PathVariable String id, @RequestParam(name="newCategoryName") String newCategoryName){
        return cuisineCategoryService.updateCategoryName(id, newCategoryName);
    }


    @ApiOperation(value = "分页列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "page", value = "current page", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "page size", required = true, dataType = "Integer")})
    @GetMapping("/pageList/{page}/{pageSize}")
    public PageVO<CuisineCategory> findByPage(@PathVariable Integer page, @PathVariable Integer pageSize){
        return new PageVO<>(cuisineCategoryService.queryCategoryPage(page,pageSize));
    }


    @ApiOperation(value = "添加分类")
    @GetMapping("/listCategoryByType/{type}")
    public List<Map<String,Object>> listCategoryByType(@PathVariable Integer type){
        return cuisineCategoryService.listCategory(type);
    }
}
