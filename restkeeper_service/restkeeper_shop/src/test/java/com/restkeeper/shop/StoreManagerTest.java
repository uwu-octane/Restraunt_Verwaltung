package com.restkeeper.shop;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.restkeeper.shop.entity.StoreManager;
import com.restkeeper.shop.service.IStoreManagerService;

import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StoreManagerTest extends BaseTest {
    @Reference(version = "1.0.0", check = false)
    private IStoreManagerService storeManagerService;

    @Test
    public void queryPageTest(){
        IPage<StoreManager> page = storeManagerService.queryPageByCriteria(1,10,"test");
        List<StoreManager> list = page.getRecords();
        System.out.println(list);
    }

    @Test
    @Rollback(false)
    public void addManager(){
        List<String> storeIds = new ArrayList<>();
        Collections.addAll(storeIds, "1851722508850921473", "1851721778228310018");
        storeManagerService.addStoreManager("Eugene Harold Krabs", "tting.tao1997@gmail.com","80020207702",storeIds);

    }

    @Test
    @Rollback(false)
    public void updateManger(){
        List<String> storeIds = new ArrayList<>();
        storeIds.add("1851720900721156097");
        storeManagerService.updateStoreManager("1851743596030513154","Harold Krabs","80020207703","tting.tao1997@gmail.com",storeIds);
    }

}
