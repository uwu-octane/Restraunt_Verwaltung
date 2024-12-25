package com.restkeeper.controller;

import com.google.common.collect.Lists;
import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.service.IDishSearchService;
import com.restkeeper.store.service.ISellCalculationService;
import com.restkeeper.vo.DishPanelVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = {"Menu Search Related Interface"})
@RestController
@RequestMapping("/cuisine")
public class CuisineController {
    @Reference(version = "1.0.0", check = false)
    private IDishSearchService dishSearchService;

    @Reference(version = "1.0.0", check = false)
    private ISellCalculationService sellCalculationService;

    @ApiOperation("search cuisine by cuisine code")
    @GetMapping("/queryByCoce/{code}/{page}/{pageSize}")
    public PageVO<DishPanelVO> queryByCode(@PathVariable String code, @PathVariable int page, @PathVariable int pageSize){
        PageVO<DishPanelVO> pageResult = new PageVO<>();

        SearchResult<DishEs> result = dishSearchService.searchCuisineByCode(code, page, pageSize);

        pageResult.setCounts(result.getTotal());
        pageResult.setPage(page);
        long pageCount = result.getTotal()%pageSize==0?result.getTotal()/pageSize : result.getTotal()/pageSize+1;
        pageResult.setPages(pageCount);
        List<DishPanelVO> cuisinePanelVOList = Lists.newArrayList();
        result.getRecords().forEach(es->{
            DishPanelVO cuisinePanelVO = new DishPanelVO();
            cuisinePanelVO.setDishId(es.getId());
            System.out.println(es.getDishName());
            cuisinePanelVO.setDishName(es.getDishName());
            cuisinePanelVO.setPrice(es.getPrice());
            cuisinePanelVO.setImage(es.getImage());
            cuisinePanelVO.setType(es.getType());
            cuisinePanelVO.setRemainder(sellCalculationService.getRemainCuisineNum(es.getId()));
            cuisinePanelVOList.add(cuisinePanelVO);
        });
        pageResult.setItems(cuisinePanelVOList);
        return pageResult;
    }
}
