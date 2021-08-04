package com.myself.oss.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.myself.oss.service.FileService;
import com.myself.oss.utils.ConstantOssPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    //上传文件
    @Override
    public String upload(MultipartFile file) {
        String endpoint = ConstantOssPropertiesUtil.ENDPOINT;
        String accessKeyId = ConstantOssPropertiesUtil.ACCESS_KEY_ID;
        String secret = ConstantOssPropertiesUtil.SECRET;
        String bucket = ConstantOssPropertiesUtil.BUCKET;
        //创建ossClient实例
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, secret);
        //上传文件流
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            //获取uuid
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            //获取日期
            String timeUrl = new DateTime().toString("yyyy/MM/dd");
            //上传到以日期为名称的文件夹中，并防止文件名重复
            String fileName = timeUrl + "/" + uuid + file.getOriginalFilename();
            //上传文件
            ossClient.putObject(bucket, fileName, inputStream);
            //关闭ossClient
            ossClient.shutdown();
            //获取上传后文件路径url
            String url = "https://" + bucket + "." + endpoint + "/" + fileName;
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
