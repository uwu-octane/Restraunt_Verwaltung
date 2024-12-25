package com.restkeeper.store.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.SetMenu;
import com.restkeeper.store.entity.SetMenuCuisine;
import com.restkeeper.store.mapper.SetMenuMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service(version = "1.0.0",protocol = "dubbo")
@org.springframework.stereotype.Service("setMenuService")
public class SetMenuServiceImpl extends ServiceImpl<SetMenuMapper, SetMenu> implements ISetMenuService {
    @Autowired
    @Qualifier("setMenuCuisineService")
    private ISetMenuCuisineService setMenuCuisineService;

    @Override
    public IPage<SetMenu> queryPage(int pageNum, int pageSize, String name) {
        IPage<SetMenu> page = new Page<>(pageNum,pageSize);

        QueryWrapper<SetMenu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotEmpty(name)){
            queryWrapper.lambda().like(SetMenu::getName,name);
        }
        return this.page(page,queryWrapper);
    }

    @Override
    public boolean addMenu(SetMenu setMenu, List<SetMenuCuisine> setMenuCuisineList) {
        this.save(setMenu);
        setMenuCuisineList.forEach(s->{
            s.setSetMealId(setMenu.getId());
            s.setIndex(0);
        });
        return setMenuCuisineService.saveBatch(setMenuCuisineList);
    }

    @Override
    @Transactional
    public boolean update(SetMenu setMenu, List<SetMenuCuisine> setMenuCuisines) {
        try{
            this.updateById(setMenu);
            if (setMenuCuisines != null || setMenuCuisines.size()>0){
                QueryWrapper<SetMenuCuisine> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SetMenuCuisine::getSetMealId, setMenu.getId());

                setMenuCuisineService.remove(queryWrapper);

                setMenuCuisines.forEach((setMenuCuisine) -> {setMenuCuisine.setSetMealId(setMenu.getId());
                });

                setMenuCuisineService.saveBatch(setMenuCuisines);
            }
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return  false;
        }

    }


}
