package com.ops.zen.fso;

import java.io.InputStream;
import java.util.List;

/**
 * @author xyn
 * @date 2026/4/27
 * @description FSO(File Storage Object) 标准接口
 */
public interface FsoService {

    /**
     * 查询桶列表
     */
    List<FsoBucket> listBuckets();

    /**
     * 获取桶详细信息
     */
    FsoBucket getBucketDetail(String bucketName);

    /**
     * 创建桶
     */
    void createBucket(String bucketName);

    /**
     * 删除桶
     */
    void deleteBucket(String bucketName);

    /**
     * 查看桶内对象(支持关键字过滤)
     */
    List<FsoObject> listObjects(String bucketName, String prefix, String keyword);

    /**
     * 获取对象详情
     */
    FsoObject getObjectDetail(String bucketName, String objectKey);

    /**
     * 下载对象
     */
    InputStream downloadObject(String bucketName, String objectKey);

    /**
     * 删除对象
     */
    void deleteObject(String bucketName, String objectKey);

    /**
     * 上传对象
     */
    void uploadObject(String bucketName, String objectKey, InputStream inputStream, long size);

    /**
     * 初始化连接
     */
    void init(FsoConfig config);
}
