package com.ops.zen.controller;

import com.ops.zen.en.DsConnTypeEn;
import com.ops.zen.en.DbTypeEn;
import com.ops.zen.en.SshLoginTypeEn;
import com.ops.zen.select.EnSelectParams;
import com.ops.zen.select.ISelectParams;
import com.ops.zen.select.SQLCustomSelectParams;
import com.ops.zen.utils.Assert;
import com.ops.zen.utils.StringUtils;
import com.ops.zen.utils.en.EnUtils;
import com.ops.zen.utils.en.SelectModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xyn
 * @date 2025/4/9 20:52
 * @description 下拉模型通用接口
 **/
@Deprecated
@RestController
@RequestMapping("/selectModel")
@Slf4j
public class SelectModelController {

    private static Map<String, ISelectParams> map = new HashMap<>();

    static {
        map.put("DsDbType", new EnSelectParams(DbTypeEn.class));
        map.put("DsConnectType", new EnSelectParams(DsConnTypeEn.class));
        map.put("SshLoginTp", new EnSelectParams(SshLoginTypeEn.class));
    }


    @GetMapping("options")
    public List<SelectModel> options(@RequestParam String selectId,
                                     @RequestParam(required = false) String keyword) {
        ISelectParams model = map.get(selectId);
        Assert.notNull(model, "根据selectId未找到符合条件的下拉框数据");
        if (model instanceof EnSelectParams) {
            EnSelectParams m = (EnSelectParams) model;
            List<SelectModel> selectModels = EnUtils.toSelectModels(m.getEnClazz());
            return StringUtils.isEmpty(keyword) ? selectModels : EnUtils.toSelectModelsFuzzy(m.getEnClazz(), keyword, m.getGroup());
        } else if (model instanceof SQLCustomSelectParams) {
            throw new RuntimeException("暂不支持");
        }
        return null;
    }



}
