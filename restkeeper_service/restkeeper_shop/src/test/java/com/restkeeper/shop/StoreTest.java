package com.restkeeper.shop;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.dto.StoreDTO;
import com.restkeeper.shop.entity.Store;
import com.restkeeper.shop.service.IStoreService;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import sun.lwawt.macosx.CSystemTray;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class StoreTest extends BaseTest{

    @Reference(version = "1.0.0",check = false)
    private IStoreService storeService;

    @Test
    @Rollback(false)
    public void saveTest(){
        Store store = new Store();
        store.setBrandId("Greek Haus");
        store.setStoreName("Greek Haus");
        store.setProvince("Niedersachsen");
        store.setCity("Braunschweig");
        store.setArea("Braunschweig");
        store.setAddress("Frankfurter Straße 268");
        store.setContact("Jamen Hussein");
        store.setContactPhone("80020207702");
        storeService.save(store);
    }

    @Test
    public void queryTest(){
        Store store = storeService.getById("1849183779395010561");
        System.out.println(store);
    }

    @Test
    public void pagequeryTest(){
        storeService.paginationQueryByName(1,2,"Krusty Krab 01");
    }

    @Test
    @Rollback(false)
    public void disableStoreTest(){
        Store store = storeService.getById("1851370800842604545");
        store.setStatus(SystemCode.FORBIDDEN);

        storeService.updateById(store);
    }

    @Test
    public void getAllProvinceTest(){
        List<String> res = storeService.getAllProvince();
        System.out.println(res);
    }

    @Test
    public void getStoreByProvinceTest(){
        List<StoreDTO> res = storeService.getStoreByProvince("Niedersachsen");
        System.out.println(res);
    }

}
