/*package com.ops.zen.cache;

import com.uis.nx.soar.base.commons.IStarter;
import com.uis.nx.soar.base.msgnoti.INotiMessage;
import com.uis.nx.soar.base.msgnoti.NotiEventTypeEn;
import com.uis.nx.soar.base.msgnoti.recv.NotiListener;
import com.uis.nx.soar.base.msgnoti.recv.SyncNotiDispatcher;

import java.io.IOException;

*//**
 * @author Ezreal
 * @date 2020/9/24 17:07
 * @description
 **//*
public class CacheStarter implements IStarter {

    @Override
    public String getName() {
        return "缓存清理监听";
    }

    public void start() {
        SyncNotiDispatcher.inst().register(NotiEventTypeEn.CLEAR_CACHE_BY_ID, new NotiListener() {
            @Override
            public void onMessage(String eventType, INotiMessage msg) throws IOException {
                String body = msg.getBody();
                String split = "===";
                if (body.contains(split)) {
                    String[] args = body.split(split);
                    CacheManager.inst().invalidate(args[0], args[1]);
                } else {
                    CacheManager.inst().invalidate(body);
                }
            }
        });
        SyncNotiDispatcher.inst().register(NotiEventTypeEn.CLEAR_CACHE_ALL, new NotiListener() {
            @Override
            public void onMessage(String eventType, INotiMessage msg) throws IOException {
                CacheManager.inst().invalidateAll();//一些没有被CacheIdEn管理的缓存也会被清理
            }
        });

        summary(getName(), new StarterSummary(null));
   }
} */
