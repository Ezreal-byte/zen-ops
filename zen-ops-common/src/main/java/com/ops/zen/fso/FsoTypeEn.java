package com.ops.zen.fso;

import com.ops.zen.utils.en.EnumDescription;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 对象存储数据源类型常量接口
 * 使用EnUtils转换为枚举，extType关联对应的Config类
 * 后续新增中间件只需：1.添加常量 2.创建Config类 3.实现FsoService接口
 */
public interface FsoTypeEn {

    @EnumDescription(remark = "MinIO", extType = MinioConfig.class)
    String MINIO = "MINIO";

    @EnumDescription(remark = "阿里云OSS", extType = AliyunOssConfig.class)
    String ALIYUN_OSS = "ALIYUN_OSS";

    @EnumDescription(remark = "RustFS", extType = RustFsConfig.class)
    String RUST_FS = "RUST_FS";
}
