package com.ops.zen.fso;

import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.PutObjectOptions;
import io.minio.Result;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xyn
 * @date 2026/4/27
 * @description MinIO FSO 实现 (minio 7.0.2, 纯Java实现, 完美兼容Java 8)
 */
@Slf4j
public class MinioFsoService implements FsoService {

    private MinioClient minioClient;

    @Override
    public void init(FsoConfig config) {
        MinioConfig c = (MinioConfig) config;
        try {
            this.minioClient = new MinioClient(c.getUrl(), c.getAccessKey(), c.getSecretKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FsoBucket> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<FsoBucket> result = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Bucket bucket : buckets) {
                FsoBucket fsoBucket = new FsoBucket();
                fsoBucket.setName(bucket.name());
                fsoBucket.setCreationDate(bucket.creationDate() != null
                        ? bucket.creationDate().withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
                        : null);
                result.add(fsoBucket);
            }
            return result;
        } catch (Exception e) {
            log.error("MinIO查询桶列表失败", e);
            throw new RuntimeException("查询桶列表失败: " + e.getMessage());
        }
    }

    @Override
    public FsoBucket getBucketDetail(String bucketName) {
        try {
            // 获取桶的基本信息
            List<Bucket> buckets = minioClient.listBuckets();
            Bucket targetBucket = null;
            for (Bucket bucket : buckets) {
                if (bucket.name().equals(bucketName)) {
                    targetBucket = bucket;
                    break;
                }
            }
            
            if (targetBucket == null) {
                throw new RuntimeException("桶不存在: " + bucketName);
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            FsoBucket fsoBucket = new FsoBucket();
            fsoBucket.setName(targetBucket.name());
            fsoBucket.setCreationDate(targetBucket.creationDate() != null
                    ? targetBucket.creationDate().withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
                    : null);
            
            // 获取对象数量和总大小
            long objectCount = 0;
            long totalSize = 0;
            Iterable<Result<Item>> results = minioClient.listObjects(bucketName);
            for (Result<Item> result : results) {
                Item item = result.get();
                objectCount++;
                totalSize += item.size();
            }
            
            fsoBucket.setObjectCount(objectCount);
            fsoBucket.setTotalSize(totalSize);
            
            return fsoBucket;
        } catch (Exception e) {
            log.error("MinIO获取桶详细信息失败: {}", bucketName, e);
            throw new RuntimeException("获取桶详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            minioClient.makeBucket(bucketName);
        } catch (Exception e) {
            log.error("MinIO创建桶失败: {}", bucketName, e);
            throw new RuntimeException("创建桶失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            minioClient.removeBucket(bucketName);
        } catch (Exception e) {
            log.error("MinIO删除桶失败: {}", bucketName, e);
            throw new RuntimeException("删除桶失败: " + e.getMessage());
        }
    }

    @Override
    public List<FsoObject> listObjects(String bucketName, String prefix, String keyword) {
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    bucketName,
                    prefix != null ? prefix : "",
                    true
            );
            List<FsoObject> fsoObjects = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Result<Item> result : results) {
                Item item = result.get();
                // 后端关键字过滤
                if (keyword != null && !keyword.isEmpty()) {
                    if (!item.objectName().contains(keyword)) {
                        continue;
                    }
                }
                FsoObject obj = new FsoObject();
                obj.setKey(item.objectName());
                obj.setSize(item.size());
                obj.setLastModified(item.lastModified() != null
                        ? item.lastModified().withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
                        : null);
                obj.setIsDir(item.isDir());
                obj.setEtag(item.etag() != null ? item.etag() : null);
                obj.setStorageClass(item.storageClass() != null ? item.storageClass() : null);
                obj.setBucketName(bucketName);
                fsoObjects.add(obj);
            }
            return fsoObjects;
        } catch (Exception e) {
            log.error("MinIO查询对象列表失败: {}/{}", bucketName, prefix, e);
            throw new RuntimeException("查询对象列表失败: " + e.getMessage());
        }
    }

    @Override
    public FsoObject getObjectDetail(String bucketName, String objectKey) {
        try {
            ObjectStat stat = minioClient.statObject(bucketName, objectKey);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            FsoObject obj = new FsoObject();
            obj.setKey(stat.name());
            obj.setSize(stat.length());
            obj.setBucketName(bucketName);
            obj.setLastModified(stat.createdTime() != null
                    ? stat.createdTime().withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
                    : null);
            obj.setEtag(stat.etag() != null ? stat.etag() : null);
            obj.setContentType(stat.contentType() != null ? stat.contentType() : null);
            return obj;
        } catch (Exception e) {
            log.error("MinIO获取对象详情失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("获取对象详情失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadObject(String bucketName, String objectKey) {
        try {
            return minioClient.getObject(bucketName, objectKey);
        } catch (Exception e) {
            log.error("MinIO下载对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("下载对象失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {
            minioClient.removeObject(bucketName, objectKey);
        } catch (Exception e) {
            log.error("MinIO删除对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("删除对象失败: " + e.getMessage());
        }
    }

    @Override
    public void uploadObject(String bucketName, String objectKey, InputStream inputStream, long size) {
        try {
            minioClient.putObject(bucketName, objectKey, inputStream, new PutObjectOptions(-1, 5 * 1024 * 1024));
        } catch (Exception e) {
            log.error("MinIO上传对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("上传对象失败: " + e.getMessage());
        }
    }
}
