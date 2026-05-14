package com.ops.zen.controller;

import com.ops.zen.entity.ZenFsoDs;
import com.ops.zen.fso.*;
import com.ops.zen.mapper.ZenFsoDsMapper;
import com.ops.zen.service.FsoDataSourceService;
import com.ops.zen.service.impl.FsoDataSourceServiceImpl;
import com.ops.zen.utils.UpDownLoader;
import com.ops.zen.utils.map.KvMap;
import com.ops.zen.utils.en.SelectModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author xyn
 * @date 2026/4/27
 * @description 对象存储数据源管理
 */
@RestController
@RequestMapping("/fso")
@Slf4j
public class FsoDataSourceController {

    @Autowired
    private FsoDataSourceService fsoDataSourceService;

    @Autowired
    private ZenFsoDsMapper fsoDataSourceMapper;

    @PostMapping("datasource/add")
    public String add(@RequestBody ZenFsoDs fsoDataSource) {
        return fsoDataSourceService.add(fsoDataSource);
    }

    @GetMapping("datasource/delete/{pkFsoDs}")
    public String delete(@PathVariable Long pkFsoDs) {
        return fsoDataSourceService.delete(pkFsoDs);
    }

    @PostMapping("datasource/update")
    public String update(@RequestBody ZenFsoDs fsoDataSource) {
        return fsoDataSourceService.update(fsoDataSource);
    }

    /**
     * 设置默认数据源（同时取消该用户其他数据源的默认状态）
     */
    @PostMapping("datasource/set-default")
    public String setDefault(@RequestParam Long pkFsoDs) {
        fsoDataSourceService.setDefault(pkFsoDs);
        return null;
    }

    @GetMapping("datasource/get/{pkFsoDs}")
    public ZenFsoDs get(@PathVariable Long pkFsoDs) {
        return fsoDataSourceService.get(pkFsoDs);
    }

    @GetMapping("datasource/list")
    public List<ZenFsoDs> list() {
        return fsoDataSourceService.listByCurrentUser();
    }

    @GetMapping("datasource/types")
    public List<Map<String, Object>> listTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        for (SelectModel sm : FsoFactory.listTypes()) {
            Map<String, Object> typeMap = new LinkedHashMap<>();
            typeMap.put("code", sm.getValue());
            typeMap.put("label", sm.getLabel());
            typeMap.put("icon", "/icons/" + sm.getValue().toLowerCase().replace("_", "-") + ".svg");
            // 从extType对应的Config类中读取FormField注解生成字段模型
            Class<?> configClazz = sm.getExtTypeClazz();
            typeMap.put("fields", buildFieldsFromConfig(configClazz));
            types.add(typeMap);
        }
        return types;
    }

    /**
     * 通过反射读取Config类上的@FormField注解，生成前端表单元数据
     */
    private List<Map<String, Object>> buildFieldsFromConfig(Class<?> configClazz) {
        List<Map<String, Object>> fields = new ArrayList<>();
        List<Field> declaredFields = new ArrayList<>(Arrays.asList(configClazz.getDeclaredFields()));
        // 按FormField.order排序
        declaredFields.sort(Comparator.comparingInt(f -> {
            FormField ff = f.getAnnotation(FormField.class);
            return ff != null ? ff.order() : Integer.MAX_VALUE;
        }));
        for (Field f : declaredFields) {
            FormField ff = f.getAnnotation(FormField.class);
            if (ff == null) continue;
            Map<String, Object> fieldMap = new LinkedHashMap<>();
            fieldMap.put("key", f.getName());
            fieldMap.put("label", ff.label());
            fieldMap.put("placeholder", ff.placeholder());
            fieldMap.put("required", ff.required());
            fieldMap.put("inputType", ff.inputType());
            fields.add(fieldMap);
        }
        return fields;
    }

    @GetMapping("datasource/test/{pkFsoDs}")
    public KvMap testConnection(@PathVariable Long pkFsoDs) {
        String error = fsoDataSourceService.testConnection(pkFsoDs);
        KvMap result = new KvMap();
        if (error == null) {
            result.put("success", true);
            result.put("message", "连接成功");
        } else {
            result.put("success", false);
            result.put("message", error);
        }
        return result;
    }

    /**
     * 使用临时数据测试连接（不保存）
     */
    @PostMapping("datasource/test-temp")
    public KvMap testConnectionWithTempData(@RequestBody ZenFsoDs ds) {
        String error = fsoDataSourceService.testConnectionWithConfig(ds);
        KvMap result = new KvMap();
        if (error == null) {
            result.put("success", true);
            result.put("message", "连接成功");
        } else {
            result.put("success", false);
            result.put("message", error);
        }
        return result;
    }

    @GetMapping("bucket/list")
    public List<FsoBucket> listBuckets(@RequestParam Long pkFsoDs) {
        FsoService fsoService = getFsoService(pkFsoDs);
        return fsoService.listBuckets();
    }

    @GetMapping("bucket/create")
    public String createBucket(@RequestParam Long pkFsoDs, @RequestParam String bucketName) {
        FsoService fsoService = getFsoService(pkFsoDs);
        fsoService.createBucket(bucketName);
        return null;
    }

    @GetMapping("bucket/delete")
    public String deleteBucket(@RequestParam Long pkFsoDs, @RequestParam String bucketName) {
        FsoService fsoService = getFsoService(pkFsoDs);
        fsoService.deleteBucket(bucketName);
        return null;
    }

    @GetMapping("bucket/detail")
    public FsoBucket getBucketDetail(@RequestParam Long pkFsoDs, @RequestParam String bucketName) {
        FsoService fsoService = getFsoService(pkFsoDs);
        return fsoService.getBucketDetail(bucketName);
    }

    @GetMapping("object/list")
    public KvMap listObjects(@RequestParam Long pkFsoDs, @RequestParam String bucketName,
                              @RequestParam(required = false) String prefix,
                              @RequestParam(required = false) String keyword) {
        FsoService fsoService = getFsoService(pkFsoDs);
        KvMap result = new KvMap();
        result.put("list", fsoService.listObjects(bucketName, prefix, keyword));
        return result;
    }

    @GetMapping("object/detail")
    public FsoObject getObjectDetail(@RequestParam Long pkFsoDs, @RequestParam String bucketName,
                                     @RequestParam String objectKey) {
        FsoService fsoService = getFsoService(pkFsoDs);
        return fsoService.getObjectDetail(bucketName, objectKey);
    }

    @GetMapping(value = "object/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void downloadObject(@RequestParam Long pkFsoDs, @RequestParam String bucketName,
                                @RequestParam String objectKey, HttpServletResponse response) {
        FsoService fsoService = getFsoService(pkFsoDs);
        try (InputStream inputStream = fsoService.downloadObject(bucketName, objectKey)) {
            String fileName = objectKey.contains("/")
                    ? objectKey.substring(objectKey.lastIndexOf("/") + 1)
                    : objectKey;
            UpDownLoader.downLoad(fileName, inputStream, response);
        } catch (Exception e) {
            log.error("下载对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("下载对象失败: " + e.getMessage());
        }
    }

    @GetMapping("object/delete")
    public String deleteObject(@RequestParam Long pkFsoDs, @RequestParam String bucketName,
                                @RequestParam String objectKey) {
        FsoService fsoService = getFsoService(pkFsoDs);
        fsoService.deleteObject(bucketName, objectKey);
        return null;
    }

    @PostMapping(value = "object/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String uploadObject(@RequestParam Long pkFsoDs,
                               @RequestParam String bucketName,
                               @RequestParam String objectKey,
                               @RequestParam("file") MultipartFile file) {
        FsoService fsoService = getFsoService(pkFsoDs);
        try (InputStream inputStream = file.getInputStream()) {
            fsoService.uploadObject(bucketName, objectKey, inputStream, file.getSize());
            return null;
        } catch (Exception e) {
            log.error("上传对象失败: {}/{}", bucketName, objectKey, e);
            throw new RuntimeException("上传对象失败: " + e.getMessage());
        }
    }

    /**
     * 获取 FsoService 实例
     */
    private FsoService getFsoService(Long pkFsoDs) {
        ZenFsoDs ds = fsoDataSourceMapper.selectById(pkFsoDs);
        if (ds == null) {
            throw new RuntimeException("数据源不存在");
        }
        FsoConfig config = FsoDataSourceServiceImpl.parseConfig(ds);
        return FsoFactory.getFsoService(pkFsoDs.toString(), ds.getType(), config);
    }
}
