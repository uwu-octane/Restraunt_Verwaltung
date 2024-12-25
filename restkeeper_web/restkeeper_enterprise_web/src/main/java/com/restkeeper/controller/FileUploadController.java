package com.restkeeper.controller;


import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.shop.service.IAzureBlobService;
import com.restkeeper.utils.Result;
import com.restkeeper.utils.ResultCode;
import io.swagger.annotations.Api;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import io.swagger.annotations.ApiImplicitParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Api(tags = {"File Upload Common Interface"})
@RestController
@Slf4j
public class FileUploadController {

    @Reference(version = "1.0.0", check = false)
    private IAzureBlobService azureBlobService;

   /* @Value("${azure.storage.connection-string}")
    private String connectionString;
    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString)
            .buildClient();
    private final Map<String, BlobContainerClient> containerClientCache = new HashMap<>();
    private BlobContainerClient getContainerClient(String containerName) {
        return containerClientCache.computeIfAbsent(containerName, name -> {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(name);
            containerClient.createIfNotExists();
            return containerClient;
        });
    }*/


    /*@PostMapping("/pictureUpload")
    public Result pictureUpload(@RequestParam("file")MultipartFile multipartFile) {
        Result result = new Result();
        String containerName = SystemCode.IMAGE_CONTAINER_NAME;
        String contentType = multipartFile.getContentType();
        long fileSize = multipartFile.getSize();
        String blobName = multipartFile.getOriginalFilename();
        try{
            BlobContainerClient containerClient = getContainerClient(containerName);
            containerClient.createIfNotExists();
            //System.out.println("containerClient: " + containerClient);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            //InputStream inputStream = new ByteArrayInputStream(data);

            blobClient.upload(multipartFile.getInputStream(), fileSize , true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            String url = blobClient.getBlobUrl();
            result.setData(url);
            result.setStatus(ResultCode.success);
            result.setDesc("upload picture success");
        } catch (Exception e){
            log.error("Failed to upload blob. Error: " + e.getMessage());
            result.setStatus(ResultCode.error);
            result.setDesc("upload picture failed");
        }
        return result;
    }*/
    @PostMapping("/pictureUpload")
    public Result pictureUpload(@RequestParam("file") MultipartFile multipartFile){
        Result result = new Result();
        String containerName = SystemCode.IMAGE_CONTAINER_NAME;
        String blobName = multipartFile.getOriginalFilename();
        long fileSize = multipartFile.getSize();
        String contentType = multipartFile.getContentType();
        try{
            /**关于 InputStream：
             •	InputStream 是不可序列化的，无法直接通过 Dubbo 进行传输。
             •	使用 ByteArrayInputStream 可以将字节数组包装成 InputStream，满足 blobClient.upload 的参数需求。
             避免在 RPC 方法中使用与 Web 容器相关的类型，例如 MultipartFile、HttpServletRequest 等。
             •	确保参数和返回值都是可序列化的，以避免序列化异常
             nputStream 只能读取一次。如果在读取过程中流已经被消耗，再次读取时将没有数据，这可能导致数据为空或长度为零的情况。。
             **/
            byte[] fileData = multipartFile.getBytes();
            String blobUrl = azureBlobService.uploadFile(containerName,blobName, fileData,fileSize,contentType);
            if (StringUtils.isNotEmpty(blobUrl)){
                result.setData(blobUrl);
                result.setStatus(ResultCode.success);
                result.setDesc("upload picture success");
            } else{
                result.setStatus(ResultCode.error);
                result.setDesc("upload picture failed");
            }
        } catch (Exception e){
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("upload picture failed");
        }
        return result;
    }


     @PostMapping("imageUploadResized")
    @ApiImplicitParam(paramType = "form", dataType = "file", name = "file", value = "上传文件", required = true)
    public Result imageUploadResized(@RequestParam("file") MultipartFile multipartFile){
        Result result = new Result();
        String containerName = SystemCode.IMAGE_CONTAINER_NAME;
        String blobName = multipartFile.getOriginalFilename();
        String contentType = multipartFile.getContentType();
        try{

            byte[] imageResizedToUpload = resizeImage(multipartFile, 200,100);
            if (imageResizedToUpload != null && imageResizedToUpload.length > 0){
                long fileSize = imageResizedToUpload.length;
                String blobUrl = azureBlobService.uploadFile(containerName,blobName, imageResizedToUpload,fileSize,contentType);
                if (StringUtils.isNotEmpty(blobUrl)){
                    result.setData(blobUrl);
                    result.setStatus(ResultCode.success);
                    result.setDesc("upload resized picture success");
                } else{
                    result.setStatus(ResultCode.error);
                    result.setDesc("upload resized picture failed");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setStatus(ResultCode.error);
            result.setDesc("upload resized picture failed");
        }
        return result;
    }

    private byte[] resizeImage(MultipartFile multipartFile, int width, int height) {
        try{
            BufferedImage originalImage = ImageIO.read(multipartFile.getInputStream());
            BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, width, height, null);
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e){
            throw new RuntimeException("error while resizing image" + e.getMessage());
        }
    }

    //todo: implement this with CDN service (but not free)
    //https://imagekit.io/blog/optimize-and-resize-images-in-azure-blob/
   /* @PostMapping("imageUploadResized")
    @ApiImplicitParam(paramType = "form", dataType = "file", name = "file", value = "上传文件", required = true)
    public Result imageUploadResized(@RequestParam("file") MultipartFile multipartFile){
        Result result = new Result();





        return result;
    }*/
}
