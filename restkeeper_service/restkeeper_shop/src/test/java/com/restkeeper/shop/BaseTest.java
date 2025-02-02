package com.restkeeper.shop;

import com.restkeeper.tenant.TenantContext;
import org.apache.dubbo.rpc.RpcContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BaseTest {

    @Before
    public void init(){

        //RpcContext.getContext().setAttachment("shopId", "40851082");
        Map<String, Object> map = new HashMap<>();
        map.put("shopId", "40851082");
        TenantContext.addAttachments(map);
    }


}
