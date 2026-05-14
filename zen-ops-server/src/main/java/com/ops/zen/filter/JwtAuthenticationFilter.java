package com.ops.zen.filter;

import com.ops.zen.entity.LoginUser;
import com.ops.zen.utils.JwtUtils;
import com.ops.zen.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 白名单接口，不需要校验 token
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/auth/login",      // 登录接口
            "/app/config",       // 应用配置接口
            "ws/ssh",            // SSH
            "/actuator/health"  // 健康检查
    );

    // 静态资源后缀，不需要校验 token
    private static final List<String> STATIC_SUFFIXES = Arrays.asList(
            ".html", ".htm",
            ".js", ".css", ".map",
            ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".ico", ".webp",
            ".svg", ".ico",
            ".woff", ".woff2", ".ttf", ".eot",
            ".json", ".xml", ".txt", ".pdf",
            ".zip", ".tar", ".gz", ".rar"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        // 根直接放行,会重定向到静态页面
        if ("/platform/".equals(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 静态资源直接放行
        if (isStaticResource(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 白名单接口直接放行
        if (isWhiteList(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = JwtUtils.getToken(request);

            // 没有 token 或 token 为空，返回 401
            if (!StringUtils.hasText(token)) {
                sendUnauthorized(response, "未登录或登录已过期");
                return;
            }

            // 校验 token
            boolean verify = JwtUtils.verify(token, response);
            if (!verify) {
                sendUnauthorized(response, "Token无效或已过期");
                return;
            }

            // 解析用户信息并设置到上下文
            LoginUser loginUser = JwtUtils.getLoginUser(token);
            UserContext.setUserContext(loginUser);

            // 校验通过，继续执行
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.debug("JWT解析失败: {}", e.getMessage());
            sendUnauthorized(response, "Token解析失败");
        } finally {
            UserContext.removeUserContext();
        }
    }

    /**
     * 判断是否是静态资源
     */
    private boolean isStaticResource(String uri) {
        for (String suffix : STATIC_SUFFIXES) {
            if (uri.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否在白名单中
     */
    private boolean isWhiteList(String uri) {
        for (String path : WHITE_LIST) {
            if (uri.endsWith(path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回 401 未授权
     */
    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String json = "{\"code\":401,\"msg\":\"" + message + "\"}";
        response.getWriter().write(json);
    }
}
