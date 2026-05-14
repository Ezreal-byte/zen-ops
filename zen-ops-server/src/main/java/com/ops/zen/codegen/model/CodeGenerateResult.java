package com.ops.zen.codegen.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xyn
 * @date 2025/4/24 16:43
 * @description
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeGenerateResult {

    private String fileName;

    private String fileContent;

}
