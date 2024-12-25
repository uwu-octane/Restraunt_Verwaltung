package com.restkeeper.controller.store;

import com.google.common.collect.Lists;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.SetMenu;
import com.restkeeper.store.entity.SetMenuCuisine;
import com.restkeeper.store.service.ISetMenuService;
import com.restkeeper.vo.store.SetMenuCuisineVO;
import com.restkeeper.vo.store.SetMenuVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(tags = {"套餐管理"})
@RestController
@RequestMapping("/setMeal")
public class SetMenuController {

    @Reference(version = "1.0.0",check = false)
    private ISetMenuService setMenuService;

    @ApiOperation("套餐分页查询")
    @GetMapping("/queryPage/{page}/{pageSize}")
    public PageVO<SetMenu> queryPage(@PathVariable("page") Integer page,
                                     @PathVariable("pageSize") Integer pageSize,
                                     @RequestParam(value="name",required=false) String name){
        return new PageVO<>(setMenuService.queryPage(page, pageSize, name));
    }

    @ApiOperation(value = "添加套餐")
    @PostMapping("/add")
    public boolean add(@RequestBody SetMenuVO setMenuVO)  {
        SetMenu setMenu = new SetMenu();
        BeanUtils.copyProperties(setMenuVO,setMenu);

        List<SetMenuCuisine> setMenuCuisineList = Lists.newArrayList();
        if(setMenuVO.getDishList() != null){
            setMenuVO.getDishList().forEach(d->{
                SetMenuCuisine setMealDish = new SetMenuCuisine();
                setMealDish.setIndex(0);
                setMealDish.setDishCopies(d.getCopies());
                setMealDish.setDishId(d.getDishId());
                setMenuCuisineList.add(setMealDish);
            });
        }
        return setMenuService.addMenu(setMenu,setMenuCuisineList);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取套餐信息")
    public SetMenuVO getMenuInfoById(@PathVariable String id){

        SetMenu setMenu = setMenuService.getById(id);
        if(setMenu==null){
            throw new RuntimeException("Menu not exist");
        }
        SetMenuVO setMealVo=new SetMenuVO();
        BeanUtils.copyProperties(setMenu, setMealVo);
        //口味列表
        List<SetMenuCuisine> setMenuCuisineList = setMenu.getDishList();
        List<SetMenuCuisineVO> setMenuCuisineVOList=new ArrayList<>();
        for (SetMenuCuisine setMenuCuisine : setMenuCuisineList) {
            SetMenuCuisineVO setMenuCuisineVO =new SetMenuCuisineVO();
            setMenuCuisineVO.setDishId(setMenuCuisine.getDishId());
            setMenuCuisineVO.setDishName(setMenuCuisine.getDishName());
            setMenuCuisineVO.setCopies(setMenuCuisine.getDishCopies());
            setMenuCuisineVOList.add(setMenuCuisineVO);
        }

        setMealVo.setDishList(setMenuCuisineVOList);
        return  setMealVo;
    }


    @PutMapping("/updateMenu")
    public boolean updateMenu(@RequestBody SetMenuVO setMenuVO){
        SetMenu setMenu = setMenuService.getById(setMenuVO.getId());

        BeanUtils.copyProperties(setMenuVO,setMenu);
        setMenu.setDishList(null);

        List<SetMenuCuisine> setMenuCuisineList = Lists.newArrayList();
        if(setMenuVO.getDishList() != null){
            setMenuVO.getDishList().forEach(d->{
                SetMenuCuisine setMenuCuisine = new SetMenuCuisine();
                setMenuCuisine.setIndex(0);
                setMenuCuisine.setDishCopies(d.getCopies());
                setMenuCuisine.setDishId(d.getDishId());
                setMenuCuisineList.add(setMenuCuisine);
            });
        }
        return setMenuService.update(setMenu,setMenuCuisineList);
    }

}
