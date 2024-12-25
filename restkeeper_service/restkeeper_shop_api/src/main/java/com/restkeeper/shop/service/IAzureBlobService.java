package com.restkeeper.shop.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface IAzureBlobService {

    String uploadFile(String containerName, String blobName, byte[] data, long length, String contentType);
    // 下载文件
    InputStream downloadFile(String containerName, String blobName);

    boolean deleteBlob(String containerName, String blobName);

   // String uploadFile(String containerName, MultipartFile multipartFile);
}
