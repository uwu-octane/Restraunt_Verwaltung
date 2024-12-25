package com.restkeeper.shop.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service(version = "1.0.0",protocol = "dubbo")

public class AzureBlobServiceImpl implements IAzureBlobService {

    private final BlobServiceClient blobServiceClient;
    private final Map<String, BlobContainerClient> containerClientCache = new HashMap<>();

    @Autowired
    public AzureBlobServiceImpl(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    private BlobContainerClient getContainerClient(String containerName) {
        return containerClientCache.computeIfAbsent(containerName, name -> {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(name);
            containerClient.createIfNotExists();
            return containerClient;
        });
    }


    @Override
    public String uploadFile(String containerName, String blobName, byte[] data, long length, String contentType) {
        String url = "";
        try{
            BlobContainerClient containerClient = getContainerClient(containerName);
            containerClient.createIfNotExists();
            System.out.println("containerClient: " + containerClient);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            InputStream inputStream = new ByteArrayInputStream(data);

            blobClient.upload(inputStream, length , true);
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
            url = blobClient.getBlobUrl();
        } catch (Exception e){
            log.error("Failed to upload blob. Error: " + e.getMessage());
            return null;
        }

        return url;
    }

    @Override
    public InputStream downloadFile(String containerName, String blobName) {
        BlobContainerClient containerClient = getContainerClient(containerName);
        return containerClient.getBlobClient(blobName).openInputStream();
    }

    @Override
        public boolean deleteBlob(String containerName, String blobName) {
        try{
            BlobContainerClient containerClient = getContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.delete();
            return true;
        } catch (Exception e) {
            log.error("Failed to delete blob. Error: " + e.getMessage());
            return false;
        }

    }


}
