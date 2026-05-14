package com.ops.zen.controller.ws.ssh;

import com.ops.zen.controller.ws.ssh.fac.DefaultSshConnCfg;
import com.ops.zen.controller.ws.ssh.fac.PasswordSshConnCfg;
import com.ops.zen.controller.ws.ssh.fac.PrvKeySshConnCfg;
import com.ops.zen.controller.ws.ssh.fac.SShConnCfgFactory;
import com.ops.zen.en.SshLoginTypeEn;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.service.SshServerService;
import com.ops.zen.utils.JsonUtils;
import com.ops.zen.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author xyn
 * @date 2025/4/12 15:18
 * @description
 **/
@Component
@Slf4j
public class SshStarter {


    @Autowired
    private SshServerService service;

    @PostConstruct
    public void init() {
        SShConnCfgFactory.inst().setSshConnCfgIdTpCreator(cfg -> {
            DefaultSshConnCfg sshcfg = JsonUtils.toObject(DefaultSshConnCfg.class, cfg);
            return getServerConfig(sshcfg);
        });
    }

    // 获取【服务器】的ssh配置
    public DefaultSshConnCfg getServerConfig(DefaultSshConnCfg sshcfg) {
        ZenSsh svr = service.get(Long.valueOf(sshcfg.getId()));
        DefaultSshConnCfg sshConnCfg = getSshConnCfg(sshcfg.getChannelType(), svr);
        sshConnCfg.setInitCmd(sshcfg.getInitCmd()); // 初始化命令
        if (StringUtils.isNotEmpty(sshcfg.getInitPath())) {
            sshConnCfg.setInitPath(sshcfg.getInitPath());
        } else if (StringUtils.isNotEmpty(svr.getInitPath())) {
            sshConnCfg.setInitPath(svr.getInitPath());
        }
        return sshConnCfg;
    }

    private DefaultSshConnCfg getSshConnCfg(String channelType, ZenSsh svr) {
        if (SshLoginTypeEn.PASSWORD.equals(svr.getLoginTp())) {
            PasswordSshConnCfg pwd = new PasswordSshConnCfg(channelType, svr.getIp(),
                    Integer.valueOf(svr.getPortSsh()), svr.getUserName(),
                    svr.getUserPwd(), svr.getName());
            pwd.setServerId(svr.getPkServer());
            return pwd;
        } else {
            PrvKeySshConnCfg prv = new PrvKeySshConnCfg(channelType, svr.getIp(),
                    Integer.valueOf(svr.getPortSsh()), svr.getUserName(),
                    svr.getPrvKey(), svr.getPrvKeyPasswd(), svr.getName());
            prv.setServerId(svr.getPkServer());
            return prv;
        }
    }
}
