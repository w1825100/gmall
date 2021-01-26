package com.atguigu.gmall.wms;

import com.atguigu.gmall.wms.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallWmsApplicationTests {
    @Autowired
    private PurchaseService purchaseService;

    @Test
    void contextLoads() {

        System.out.println(purchaseService);
        System.out.println(purchaseService.getClass());

    }

}
