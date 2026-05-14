package com.ops.zen.fso;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 阿里云OSS FSO 实现
 */
@Slf4j
public class AliyunOssFsoService implements FsoService {

    private OSS ossClient;

    @Override
    public void init(FsoConfig config) {
        AliyunOssConfig c = (AliyunOssConfig) config;
        this.ossClient = new OSSClientBuilder().build(
                c.getEndpoint(),
                c.getAccessKeyId(),
                c.getAccessKeySecret()
        );
    }

    @Override
    public List<FsoBucket> listBuckets() {
        try {
            List<Bucket> buckets = ossClient.listBuckets();
            List<FsoBucket> result = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Bucket bucket : buckets) {
                FsoBucket fsoBucket = new FsoBucket();
                fsoBucket.setName(bucket.getName());
                fsoBucket.setCreationDate(bucket.getCreationDate() != null
                        ? sdf.format(bucket.getCreationDate())
                        : null);
                fsoBucket.setRegion(bucket.getRegion());
                result.add(fsoBucket);
            }
            return result;
        } catch (Exception e) {
            log.error("阿里云OSS查询桶列表失败", e);
            throw new RuntimeException("查询桶列表失败: " + e.getMessage());
        }
    }

    @Override
    public FsoBucket getBucketDetail(String bucketName) {
        try {
            // 获取桶的基本信息
            List<Bucket> buckets = ossClient.listBuckets();
            Bucket targetBucket = null;
            for (Bucket bucket : buckets) {
                if (bucket.getName().equals(bucketName)) {
                    targetBucket = bucket;
                    break;
                }
            }
            
            if (targetBucket == null) {
                throw new RuntimeException("桶不存在: " + bucketName);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            FsoBucket fsoBucket = new FsoBucket();
            fsoBucket.setName(targetBucket.getName());
            fsoBucket.setCreationDate(targetBucket.getCreationDate() != null
                    ? sdf.format(targetBucket.getCreationDate())
                    : null);
            fsoBucket.setRegion(targetBucket.getRegion());
            
            // 获取对象数量和总大小
            long objectCount = 0;
            long totalSize = 0;
            ObjectListing objectListing = ossClient.listObjects(bucketName);
            List<OSSObjectSummary> summaries = objectListing.getObjectSummaries();
            for (OSSObjectSummary summary : summaries) {
                objectCount++;
                totalSize += summary.getSize();
            }
            
            // 处理分页 - 使用Marker方式
            while (objectListing.isTruncated()) {
                String nextMarker = objectListing.getNextMarker();
                objectListing = ossClient.listObjects(bucketName, nextMarker);
                summaries = objectListing.getObjectSummaries();
                for (OSSObjectSummary summary : summaries) {
                    objectCount++;
                    totalSize += summary.getSize();
                }
            }
            
            fsoBucket.setObjectCount(objectCount);
            fsoBucket.setTotalSize(totalSize);
            
            return fsoBucket;
        } catch (Exception e) {
            log.error("阿里云OSS获取桶详细信息失败: {}", bucketName, e);
            throw new RuntimeException("获取桶详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            ossClient.createBucket(bucketName);
        } catch (Exception e) {
            log.error("阿里云OSS创建桶失败: {}", bucketName, e);
            throw new RuntimeException("创建桶失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            ossClient.deleteBucket(bucketName);
        } catch (Exception e) {
            log.error("阿里云OSS删除桶失败: {}", bucketName, e);
            throw new RuntimeException("删除桶失败: " + e.getMessage());
        }
    }

    @Override
    public List<FsoObject> listObjects(String bucketName, String prefix, String keyword) {
        try {
            ObjectListing objectListing = ossClient.listObjects(bucketName, prefix != null ? prefix : "");
            List<OSSObjectSummary> summaries = objectListing.getObjectSummaries();
            List<FsoObject> fsoObjects = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (OSSObjectSummary summary : summaries) {
                // 后端关键字过滤
                if (keyword != null && !keyword.isEmpty()) {
                    if (!summary.getKey().contains(keyword)) {
                        continue;
                    }
                }
                FsoObject obj = new FsoObject();
                obj.setKey(summary.getKey());
                obj.setSize(summary.getSize());
                obj.setLastModified(summary.getLastModified() != null
                        ? sdf.format(summary.getLastModified())
                        : null);
                obj.setIsDir(false);
                obj.setEtag(summary.getETag() != null ? summary.getETag() : null);
                obj.setStorageClass(summary.getStorageClass() != null ? summary.getStorageClass() : null);
                obj.setBucketName(bucketName);
                fsoObjects.add(obj);
            }
            return fsoObjects;
        } catch (Exception e) {
            log.error("阿里云OSS查询对象列表失败: {}/{}", bucketName, prefix, e);
            throw new RuntimeException("查询对象列表失败: " + e.getMessage());
        }
    }

    @Override
    public FsoObject getObjectDetail(String bucketName, String objectKey) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectKey);
            ObjectMetadata metadata = ossObject.getObjectMetadata();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            FsoObject obj = new FsoObject();
            obj.setKey(objectKey);
            obj.setSize(metadata.getContentLength());
            obj.setBucketName(bucketName);
            obj.setLastModified(metadata.getLastModified() != null
                    ? sdf.format(metadata.getLastModified())
                    : null);
            obj.setEtag(metadata.getETag() != null ? metadata.getETag() : null);
            obj.setContentType(metadata.getContentType() != null ? metadata.getContentType() : null);
            obj.setStorageClass(metadata.getObjectStorageClass() != null ? metadata.getObjectStorageClass().name() : metadata.getRawMetadata().get("x-oss-storage-class").toString());
            // 关闭流
            ossObject.getObjectContent().close();
            return obj;
        } catch (Exception e) {
            log.error("阿里云OSS获取对象详情失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("获取对象详情失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadObject(String bucketName, String objectKey) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectKey);
            return ossObject.getObjectContent();
        } catch (Exception e) {
            log.error("阿里云OSS下载对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("下载对象失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {
            ossClient.deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            log.error("阿里云OSS删除对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("删除对象失败: " + e.getMessage());
        }
    }

    @Override
    public void uploadObject(String bucketName, String objectKey, InputStream inputStream, long size) {
        try {
            ossClient.putObject(bucketName, objectKey, inputStream);
        } catch (Exception e) {
            log.error("阿里云OSS上传对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("上传对象失败: " + e.getMessage());
        }
    }
}
