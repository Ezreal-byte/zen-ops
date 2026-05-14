package com.ops.zen.controller.ws.ssh.jsch;


import com.ops.zen.utils.en.EnumDescription;

/**
 * ssh终端类型
 *
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public interface TermTypeEn {

    @EnumDescription(remark = "不支持的类型，直接输出文本，没有颜色和特殊格式或其他控制指令等")
    String UN_SUPPORT = "UN_SUPPORT";

    @EnumDescription(remark = "")
    String ANSI = "ansi";

    @EnumDescription(remark = "")
    String VT100 = "vt100";

    @EnumDescription(remark = "")
    String VT102 = "vt102";

    @EnumDescription(remark = "")
    String VT200 = "vt200";

    @EnumDescription(remark = "")
    String VT220 = "vt220";

    @EnumDescription(remark = "")
    String XTERM = "xterm";


}
