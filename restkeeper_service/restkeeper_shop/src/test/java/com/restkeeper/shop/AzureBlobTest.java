package com.restkeeper.shop;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.service.IAzureBlobService;
import com.restkeeper.shop.service.IBrandService;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AzureBlobTest {
    @Reference(version = "1.0.0", check = false)
    private IAzureBlobService azureBlobService;

    @Test
    public void uploadTest(){
        String containerName = SystemCode.IMAGE_CONTAINER_NAME;
        String filePath = "/Users/taoting/Documents/testImage2.jpg";
        String blobName = "testImage2.jpg";
        String contentType = "image/jpeg";

        File file = new File(filePath);
        try(InputStream inputStream = new FileInputStream(file)){
            long fileSize = file.length();
            //azureBlobService.uploadFile(containerName,blobName,inputStream,fileSize,contentType);
            System.out.println("File uploaded successfully: " + blobName);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Test
    public void deleteTest(){
        String blobName = "testImage2.jpg";
        String containerName = SystemCode.IMAGE_CONTAINER_NAME;
        boolean res =  azureBlobService.deleteBlob(containerName,blobName);
        System.out.println(res);
    }
}
