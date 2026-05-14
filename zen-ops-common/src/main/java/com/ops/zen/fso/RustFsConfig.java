package com.ops.zen.fso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2026/4/27
 * @description RustFS 数据源配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RustFsConfig implements FsoConfig {

    @FormField(label = "Endpoint", required = true, placeholder = "http://127.0.0.1:9000", order = 1)
    private String endpoint;

    @FormField(label = "Access Key", required = true, placeholder = "请输入Access Key", order = 2)
    private String accessKey;

    @FormField(label = "Secret Key", required = true, placeholder = "请输入Secret Key", inputType = "password", order = 3)
    private String secretKey;

    @FormField(label = "Region", required = false, placeholder = "us-east-1", order = 4)
    private String region;
}
