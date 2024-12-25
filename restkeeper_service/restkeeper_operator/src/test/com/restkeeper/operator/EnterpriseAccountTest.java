package com.restkeeper.operator;

import com.restkeeper.operator.service.IEnterpriseAccountService;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.SpringVersion;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EnterpriseAccountTest {

    @Reference(version = "1.0.0", check = false)
    private IEnterpriseAccountService enterpriseAccountService;

    @Test
    @Rollback(false)
    public void delTest(){
        enterpriseAccountService.removeById("1848003780008738817");
    }

}
