package com.fc.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.List;

@Data
@AllArgsConstructor
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     * 文件上传
     *
     * @param bytes
     * @param objectName
     * @return
     */
    public String upload(byte[] bytes, String objectName) {

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 创建PutObject请求。
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        //文件访问路径规则 https://BucketName.Endpoint/ObjectName
        StringBuilder stringBuilder = new StringBuilder("https://");
        stringBuilder
                .append(bucketName)
                .append(".")
                .append(endpoint)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }

    /**
     * 文件删除
     * @param fileUrl 文件完整URL
     * @return 是否删除成功
     */
    public boolean delete(String fileUrl) {
        // 从完整URL中提取objectName
        String objectName = extractObjectNameFromUrl(fileUrl);
        if (objectName == null) {
            log.error("无法从URL中提取objectName: {}", fileUrl);
            return false;
        }

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 检查文件是否存在
            boolean exists = ossClient.doesObjectExist(bucketName, objectName);
            if (!exists) {
                log.warn("文件不存在，无需删除: {}", objectName);
                return true;
            }

            // 删除文件
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件删除成功: {}", objectName);
            return true;

        } catch (OSSException oe) {
            log.error("OSS异常，删除文件失败: {}", objectName, oe);
            return false;
        } catch (ClientException ce) {
            log.error("客户端异常，删除文件失败: {}", objectName, ce);
            return false;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 从完整URL中提取objectName
     * 格式: https://BucketName.Endpoint/ObjectName
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        try {
            // 移除协议头
            String urlWithoutProtocol = fileUrl.replace("https://", "").replace("http://", "");

            // 找到第一个斜杠的位置，斜杠后面的就是objectName
            int firstSlashIndex = urlWithoutProtocol.indexOf("/");
            if (firstSlashIndex == -1) {
                return null;
            }

            return urlWithoutProtocol.substring(firstSlashIndex + 1);
        } catch (Exception e) {
            log.error("解析URL失败: {}", fileUrl, e);
            return null;
        }
    }

    /**
     * 批量删除文件
     * @param fileUrls 文件URL列表
     * @return 成功删除的文件数量
     */
    public int batchDelete(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String fileUrl : fileUrls) {
            if (delete(fileUrl)) {
                successCount++;
            }
        }

        log.info("批量删除完成: 成功{}/{}", successCount, fileUrls.size());
        return successCount;
    }
}
