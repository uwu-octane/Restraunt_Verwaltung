package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.store.entity.CuisineFlavor;
import com.restkeeper.store.mapper.CuisineFlavorMapper;
import org.apache.dubbo.config.annotation.Service;

@Service(version = "1.0.0", protocol = "dubbo")
@org.springframework.stereotype.Service("cuisineFlavorService")
public class CuisineFlavorServiceImpl extends ServiceImpl<CuisineFlavorMapper, CuisineFlavor> implements ICuisineFlavorService {

}
