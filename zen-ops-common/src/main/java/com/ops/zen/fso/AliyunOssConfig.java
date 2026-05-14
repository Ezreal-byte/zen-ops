package com.ops.zen.fso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 阿里云OSS 数据源配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AliyunOssConfig implements FsoConfig {

    @FormField(label = "Endpoint", required = true, placeholder = "oss-cn-hangzhou.aliyuncs.com", order = 1)
    private String endpoint;

    @FormField(label = "Access Key ID", required = true, placeholder = "请输入Access Key ID", order = 2)
    private String accessKeyId;

    @FormField(label = "Access Key Secret", required = true, placeholder = "请输入Access Key Secret", inputType = "password", order = 3)
    private String accessKeySecret;

//    @FormField(label = "Region", required = true, placeholder = "cn-hangzhou", order = 4)
//    private String region;
}
