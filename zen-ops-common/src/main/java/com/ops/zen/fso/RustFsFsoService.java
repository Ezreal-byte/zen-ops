package com.ops.zen.fso;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xyn
 * @date 2026/4/27
 * @description RustFS FSO 实现 (基于 AWS S3 SDK v2)
 */
@Slf4j
public class RustFsFsoService implements FsoService {

    private S3Client s3Client;

    @Override
    public void init(FsoConfig config) {
        RustFsConfig c = (RustFsConfig) config;
        try {
            this.s3Client = S3Client.builder()
                    .endpointOverride(URI.create(c.getEndpoint()))
                    .region(c.getRegion() != null && !c.getRegion().isEmpty()
                            ? Region.of(c.getRegion())
                            : Region.US_EAST_1)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(c.getAccessKey(), c.getSecretKey())
                    ))
                    .forcePathStyle(true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<FsoBucket> listBuckets() {
        try {
            ListBucketsResponse response = s3Client.listBuckets();
            List<FsoBucket> result = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Bucket bucket : response.buckets()) {
                FsoBucket fsoBucket = new FsoBucket();
                fsoBucket.setName(bucket.name());
                fsoBucket.setCreationDate(bucket.creationDate() != null
                        ? bucket.creationDate().atZone(ZoneId.systemDefault()).format(formatter)
                        : null);
                result.add(fsoBucket);
            }
            return result;
        } catch (Exception e) {
            log.error("RustFS查询桶列表失败", e);
            throw new RuntimeException("查询桶列表失败: " + e.getMessage());
        }
    }

    @Override
    public FsoBucket getBucketDetail(String bucketName) {
        try {
            // 获取桶的基本信息
            ListBucketsResponse response = s3Client.listBuckets();
            Bucket targetBucket = null;
            for (Bucket bucket : response.buckets()) {
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
                    ? targetBucket.creationDate().atZone(ZoneId.systemDefault()).format(formatter)
                    : null);
            
            // 获取对象数量和总大小
            long objectCount = 0;
            long totalSize = 0;
            ListObjectsV2Request.Builder listRequestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName);
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequestBuilder.build());
            for (S3Object s3Object : listResponse.contents()) {
                objectCount++;
                totalSize += s3Object.size();
            }
            
            // 处理分页
            while (listResponse.isTruncated()) {
                listRequestBuilder = ListObjectsV2Request.builder()
                        .bucket(bucketName)
                        .continuationToken(listResponse.nextContinuationToken());
                listResponse = s3Client.listObjectsV2(listRequestBuilder.build());
                for (S3Object s3Object : listResponse.contents()) {
                    objectCount++;
                    totalSize += s3Object.size();
                }
            }
            
            fsoBucket.setObjectCount(objectCount);
            fsoBucket.setTotalSize(totalSize);
            
            return fsoBucket;
        } catch (Exception e) {
            log.error("RustFS获取桶详细信息失败: {}", bucketName, e);
            throw new RuntimeException("获取桶详细信息失败: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (BucketAlreadyExistsException | BucketAlreadyOwnedByYouException e) {
            log.warn("RustFS桶已存在: {}", bucketName);
        } catch (Exception e) {
            log.error("RustFS创建桶失败: {}", bucketName, e);
            throw new RuntimeException("创建桶失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("RustFS删除桶失败: {}", bucketName, e);
            throw new RuntimeException("删除桶失败: " + e.getMessage());
        }
    }

    @Override
    public List<FsoObject> listObjects(String bucketName, String prefix, String keyword) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucketName);
            if (prefix != null && !prefix.isEmpty()) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());
            List<FsoObject> fsoObjects = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (S3Object s3Object : response.contents()) {
                // 后端关键字过滤
                if (keyword != null && !keyword.isEmpty()) {
                    if (!s3Object.key().contains(keyword)) {
                        continue;
                    }
                }
                FsoObject obj = new FsoObject();
                obj.setKey(s3Object.key());
                obj.setSize(s3Object.size());
                obj.setLastModified(s3Object.lastModified() != null
                        ? s3Object.lastModified().atZone(ZoneId.systemDefault()).format(formatter)
                        : null);
                obj.setIsDir(false);
                obj.setEtag(s3Object.eTag());
                obj.setStorageClass(s3Object.storageClass() != null ? s3Object.storageClass().name() : null);
                obj.setBucketName(bucketName);
                fsoObjects.add(obj);
            }
            return fsoObjects;
        } catch (Exception e) {
            log.error("RustFS查询对象列表失败: {}/{}", bucketName, prefix, e);
            throw new RuntimeException("查询对象列表失败: " + e.getMessage());
        }
    }

    @Override
    public FsoObject getObjectDetail(String bucketName, String objectKey) {
        try {
            HeadObjectResponse response = s3Client.headObject(
                    HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build()
            );
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            FsoObject obj = new FsoObject();
            obj.setKey(objectKey);
            obj.setSize(response.contentLength());
            obj.setBucketName(bucketName);
            obj.setLastModified(response.lastModified() != null
                    ? response.lastModified().atZone(ZoneId.systemDefault()).format(formatter)
                    : null);
            obj.setEtag(response.eTag());
            obj.setContentType(response.contentType());
            return obj;
        } catch (Exception e) {
            log.error("RustFS获取对象详情失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("获取对象详情失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadObject(String bucketName, String objectKey) {
        try {
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(objectKey).build()
            );
            return response;
        } catch (Exception e) {
            log.error("RustFS下载对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("下载对象失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteObject(String bucketName, String objectKey) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(objectKey).build());
        } catch (Exception e) {
            log.error("RustFS删除对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("删除对象失败: " + e.getMessage());
        }
    }

    @Override
    public void uploadObject(String bucketName, String objectKey, InputStream inputStream, long size) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucketName).key(objectKey).build(),
                    RequestBody.fromInputStream(inputStream, size)
            );
        } catch (Exception e) {
            log.error("RustFS上传对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("上传对象失败: " + e.getMessage());
        }
    }
}
