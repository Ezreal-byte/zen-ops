package com.ops.zen.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ops.zen.entity.LoginUser;
import com.ops.zen.utils.ex.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaoyingnan
 * @version 2021/04/16 13:00
 * <JWT工具类-控制层>
 **/
public class JwtUtils {

    private static Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    /**
     * token过期时间
     *
     * @select 1000 * 60 * 60 * 24 一天
     * @select 1000 * 60 * 30 半小时
     * @select 1000 * 60 * 15 15分钟
     */
    private static final long EXPIRE_TIME = 1000 * 60 * 15;

    private static final long GEN_NEW_TOKEN_TIME = 1000 * 60 * 5;//token临近过期需要续签的阈值  5分钟

    /*** token私钥*/
    private static final String TOKEN_SECRET = "111222";


    /**
     * 生成token 设置过期时间  将部分信息加密到token中
     *
     * @return
     */
    public static String getToken(LoginUser loginUser) {
        // 过期时间
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // 私钥及加密算法
        Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
        // 设置头部信息
        Map<String, Object> header = new HashMap<>(2);
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        // 附带用户信息，生成签名
        String token = JWT.create().withHeader(header).withPayload(loginUser.toMap())
                //要往token中存储的东西
                .withExpiresAt(date).sign(algorithm);
        return token;

    }

    public static LoginUser getLoginUser(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isBlank(token))
            throw new RuntimeException("该用户未登录!");
        return getLoginUser(token);
    }

    public static LoginUser getLoginUser(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Map<String, String> map = getMap(jwt);
        return new LoginUser(map);
    }

    /**
     * 验证token是否正确
     *
     * @param token
     * @param response
     * @return
     */
    public static boolean verify(String token, HttpServletResponse response) {
        try {
            // TODO 单例，减少验证过程对象多次创建
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
//            DecodedJWT jwt1 = JWT.decode(token);
            if ((jwt.getExpiresAt().getTime() - GEN_NEW_TOKEN_TIME) < System.currentTimeMillis()) {//如果过期时间减去五分钟小于当前时间  生成新token
//                System.err.println(DateTimeUtils.normalFormatDate(jwt.getExpiresAt()));
                // 设置头部信息
                Map<String, Object> header = new HashMap<>(2);
                header.put("typ", "JWT");
                header.put("alg", "HS256");
                // 过期时间
                Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
                String newToken = JWT.create().withHeader(header).withPayload(getMap(jwt)).withExpiresAt(date).sign(algorithm);
                response.setHeader(JWT_RESPONSE_HEADER_TOKEN, newToken);
            }
            return true;
        } catch (Exception exception) {
            logger.debug("token鉴权失败,异常信息:{}", Exceptions.trace(exception));
            return false;
        }
    }

    /**
     * 根据key取出token中存储的信息
     *
     * @param token
     * @param key
     * @return
     */
    public static String get(String token, String key) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim(key).asString();
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    /**
     * 根据key取出token中存储的信息
     *
     * @param request
     * @param key
     * @return
     */
    public static String get(HttpServletRequest request, String key) {
        String token = getToken(request);
        if (StringUtils.isBlank(token))
            throw new RuntimeException("该用户未登录!");
        return get(token, key);
    }


    public static Map<String, String> getMap(DecodedJWT jwt) {
        Map<String, Claim> claimMap = jwt.getClaims();
        Map<String, String> map = new HashMap<>(claimMap.size());
        for (Map.Entry<String, Claim> entry : claimMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().asString());
        }
        return map;
    }

    /**
     * JWT请求头的token key
     */
    private static final String JWT_REQUEST_HEADER_TOKEN = "Authentication-Token";
    private static final String JWT_REQUEST_PARAMETER_TOKEN = "token";
    /**
     * JWT响应头的token key
     */
    public static final String JWT_RESPONSE_HEADER_TOKEN = "token";

    public static String getToken(HttpServletRequest req) {
        /**
         * 优先取header，后取parameter
         */
        String token = req.getHeader(JWT_REQUEST_HEADER_TOKEN);
        if (StringUtils.isEmpty(token)) {
            token = req.getParameter(JWT_REQUEST_PARAMETER_TOKEN);
        }
        return token;
    }
}
