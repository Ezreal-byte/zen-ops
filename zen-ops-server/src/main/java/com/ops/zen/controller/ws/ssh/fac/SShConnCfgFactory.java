package com.ops.zen.controller.ws.ssh.fac;

import com.ops.zen.utils.Assert;
import com.ops.zen.utils.JsonUtils;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class SShConnCfgFactory {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(SShConnCfgFactory.class);

    private volatile static SShConnCfgFactory _inst;

    private Function<String, SshConnCfg> cfgIdTpCreator;

    private SShConnCfgFactory() {
    }

    public static SShConnCfgFactory inst() {
        if (_inst == null) {
            synchronized (SShConnCfgFactory.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new SShConnCfgFactory();
            }
        }
        return _inst;
    }

    /**
     * 自定义的SshConnCfg创建器
     *
     * @param cfgIdTpCreator
     */
    public void setSshConnCfgIdTpCreator(Function<String, SshConnCfg> cfgIdTpCreator) {
        this.cfgIdTpCreator = cfgIdTpCreator;
    }

    /**
     * cfgDes为如下的content部分的字符串形式
     * <pre>
     *     1.
     *     {
     *           op: 'connect',
     *           content:  {
     *             channelType: 'shell',
     *             type: 'PL_PWD', //
     *             host: '172.16.2.28', // IP
     *             port: '23432', // 端口号
     *             username: 'root', // 用户名
     *             password: 'xxx'// 密码
     *           }
     *      }
     *      2.
     *     {
     *           op: 'connect',
     *           content:  {
     *             channelType: 'shell',
     *             type: 'PL_PRV_KEY', //
     *             host: '172.16.2.28', // IP
     *             port: '23432', // 端口号
     *             username: 'root', // 用户名
     *             passphrase: 'xxx',// 私钥密码
     *             prvKey: 'xxx'// 私钥
     *           }
     *      }
     *      3.
     *     {
     *           op: 'connect',
     *           content:  {
     *             channelType: 'shell',
     *             type: 'ID', // ID
     *             id: 'xxx',
     *             idType:'xxx' // 自定义的id类型，标识type等于ID时id的类型
     *           }
     *      }
     *
     * </pre>
     *
     * @param cfgDes
     * @return
     */
    public SshConnCfg create(String cfgDes) {
        DefaultSshConnCfg dcfg = JsonUtils.toObject(DefaultSshConnCfg.class, cfgDes);
        String type = String.valueOf(dcfg.getType());
        if (SshConnCfgTpEn.PLAINTEXT_PWD.equals(type)) {
            return JsonUtils.toObject(PasswordSshConnCfg.class, cfgDes);
        } else if (SshConnCfgTpEn.PLAINTEXT_PRV_KEY.equals(type)) {
            return JsonUtils.toObject(PrvKeySshConnCfg.class, cfgDes);
        } else if (SshConnCfgTpEn.ID.equals(type)) {
            Assert.notNull(this.cfgIdTpCreator, "当type为ID时，cfgIdTpCreator不能为空");
            return this.cfgIdTpCreator.apply(cfgDes);
        } else {
            throw new RuntimeException("未知的SSH连接类型：" + type);
        }
    }
}
