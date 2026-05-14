package com.ops.zen.fso;

import com.ops.zen.utils.en.EnUtils;
import com.ops.zen.utils.en.SelectModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xyn
 * @date 2026/4/27
 * @description FSO 工厂类，根据数据源类型动态创建对应的 FsoService 实例
 * 通过 EnUtils 读取 FsoTypeEn 的 @EnumDescription.extType 获取Config类
 */
public class FsoFactory {

    private static final Map<String, FsoService> CACHE = new ConcurrentHashMap<>();

    /**
     * 获取 FsoService 实例（带缓存）
     */
    public static FsoService getFsoService(String pkFsoDs, String type, FsoConfig config) {
        return CACHE.computeIfAbsent(pkFsoDs, key -> {
            FsoService fsoService = createFsoService(type);
            fsoService.init(config);
            return fsoService;
        });
    }

    /**
     * 创建新的 FsoService 实例（不使用缓存，用于测试连接）
     */
    public static FsoService createFsoService(String type, FsoConfig config) {
        FsoService fsoService = createFsoService(type);
        fsoService.init(config);
        return fsoService;
    }

    /**
     * 根据类型创建FsoService实例
     */
    private static FsoService createFsoService(String type) {
        if (FsoTypeEn.MINIO.equals(type)) {
            return new MinioFsoService();
        } else if (FsoTypeEn.ALIYUN_OSS.equals(type)) {
            return new AliyunOssFsoService();
        } else if (FsoTypeEn.RUST_FS.equals(type)) {
            return new RustFsFsoService();
        }
        throw new RuntimeException("不支持的对象存储类型: " + type);
    }

    /**
     * 移除缓存（配置变更时调用）
     */
    public static void removeCache(String pkFsoDs) {
        CACHE.remove(pkFsoDs);
    }

    /**
     * 根据类型获取对应的Config类
     * 从FsoTypeEn的@EnumDescription.extType中获取
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends FsoConfig> getConfigClass(String type) {
        SelectModel sm = EnUtils.enDes(FsoTypeEn.class, type);
        if (sm != null && sm.getExtTypeClazz() != null && FsoConfig.class.isAssignableFrom(sm.getExtTypeClazz())) {
            return (Class<? extends FsoConfig>) sm.getExtTypeClazz();
        }
        throw new RuntimeException("未找到类型 [" + type + "] 对应的Config类，请检查FsoTypeEn的@EnumDescription.extType配置");
    }

    /**
     * 获取所有中间件类型列表
     */
    public static List<SelectModel> listTypes() {
        return EnUtils.toSelectModels(FsoTypeEn.class);
    }
}
