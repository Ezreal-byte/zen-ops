package com.ops.zen.entity.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.json.Number2BooleanDeserializer;
import com.ops.zen.json.Number2BooleanSerializer;
import lombok.Data;

/**
 * @author xyn
 * @date 2025/4/11 15:45
 * @description
 **/
@Data
public class ZenSshListResponse extends ZenSsh {

    private String loginTpName;

    /**
     * 权限主键
     */
    private String pkServerPremiss;

    /**
     * 权限类型(管理员\被分享)
     */
    private String premissType;

    private String premissTypeName;

    /**
     * 是否允许SSH连接
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isAllowSsh;

    /**
     * 是否允许SFTP连接
     */
    @JsonSerialize(using = Number2BooleanSerializer.class)
    @JsonDeserialize(using = Number2BooleanDeserializer.class)
    private Byte isAllowSftp;
}
