package com.ops.zen.fso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2026/4/27
 * @description MinIO 数据源配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinioConfig implements FsoConfig {

    @FormField(label = "URL", required = true, placeholder = "http://127.0.0.1:9000", order = 1)
    private String url;

    @FormField(label = "Access Key", required = true, placeholder = "请输入Access Key", order = 2)
    private String accessKey;

    @FormField(label = "Secret Key", required = true, placeholder = "请输入Secret Key", inputType = "password", order = 3)
    private String secretKey;
}
