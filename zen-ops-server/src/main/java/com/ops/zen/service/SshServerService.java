package com.ops.zen.service;

import com.ops.zen.entity.LoginUser;
import com.ops.zen.entity.ZenSsh;
import com.ops.zen.entity.response.ZenSshListResponse;
import com.ops.zen.utils.map.PageResult;

/**
 * @author xyn
 * @date 2025/4/11 15:02
 * @description
 **/
public interface SshServerService {
    String add(ZenSsh sshServer, LoginUser loginUser);

    String delete(Long pkServer, LoginUser loginUser);

    String update(ZenSsh sshServer, LoginUser loginUser);

    PageResult<ZenSshListResponse> list(ZenSsh sshServer, Integer pageNum, Integer pageSize, LoginUser loginUser);

    ZenSshListResponse get(Long pkServer, LoginUser loginUser);

    ZenSsh get(Long pkServer);
}
