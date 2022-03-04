package com.example.emos.wx.config.shiro;


import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
public class OAuth2Filter extends AuthenticatingFilter {
    @Autowired
    private ThreadLocalToken threadLocal;

    @Value("${emos.jwt.cache-expire}")
    private int cacheExpire;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 拦截请求后，用于把令牌字符串封装成令牌对象
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest rep = (HttpServletRequest) servletRequest;
        String token = getRequestToken(rep);

        if (StringUtils.isBlank(token)){
            return null;
        }
        return new OAuth2Token(token);
    }

    /**
     *  拦截请求，判断请求是否需要被shiro处理
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest rep = (HttpServletRequest) request;
        //AJAX提交application/json数据时，会先发出Options请求
        //如果是Option请求，就放行，不被shiro处理
        if(rep.getMethod().equals(RequestMethod.OPTIONS.name())){
            return true;
        }
        //除如上，都要被处理
        return false;
    }

    /**
     * 该方法用于处理所有应该被shiro处理的请求
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest rep = (HttpServletRequest) servletRequest;
        HttpServletResponse resp = (HttpServletResponse) servletResponse;

        //设置返回的格式和字符集
        resp.setHeader("Content-Type","text/html;charset=UTF-8");
        //允许跨域
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Origin",rep.getHeader("Origin"));

        threadLocal.clear();
        //获取请求token，如果token不存在，直接返回404
        String token = getRequestToken(rep);
        if (StringUtils.isBlank(token)){
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().println("无效令牌");
            return false;
        }

        //如果存在，检查是否过期
        try {
            jwtUtil.verifierToken(token);
        } catch (TokenExpiredException e) {
            //客户端令牌过期报错：
            //客户端令牌过期，查询redis中是否存在令牌，如果存在就生成一个新的令牌
            if (redisTemplate.hasKey(token)){
                //删除旧临牌
                redisTemplate.delete(token);
                //通过此临牌获取UserId
                int userId = jwtUtil.getUserId(token);
                //生成新令牌
                token = jwtUtil.CreateToken(userId);
                //把新令牌保存到redis中并设置有效时间
                redisTemplate.opsForValue().set(token,userId+"",cacheExpire, TimeUnit.DAYS);
                //把新的临牌绑定到线程中
                threadLocal.setToken(token);
            }else {
                //如果redis中也不存在，让用户重新登陆
                resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
                resp.getWriter().println("令牌过期");
                return false;
            }
        }catch (Exception e){
            //无效令牌报错：
            resp.setStatus(HttpStatus.SC_UNAUTHORIZED);
            resp.getWriter().println("无效令牌");
            return false;
        }

        //执行OAuth2Realm中的授权和认证
        boolean b = executeLogin(rep, resp);
        return b;
    }

    /**
     * 如果认证失败就执行此方法
     * @param token
     * @param e
     * @param request
     * @param response
     * @return
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletRequest rep = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        //设置返回的格式和字符集
        resp.setHeader("Content-Type","text/html;charset=UTF-8");
        //允许跨域
        resp.setHeader("Access-Control-Allow-Credentials","true");
        resp.setHeader("Access-Control-Allow-Origin",rep.getHeader("Origin"));

        try {
            resp.getWriter().println(e.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 获取请求令牌
     * @param rep
     * @return
     */
    public String getRequestToken(HttpServletRequest rep){
        //从header中获取token
        String token = rep.getHeader("token");
        if (StringUtils.isBlank(token)){
            //如果为空或者空字符串，就从请求体中获取
            token = rep.getParameter("token");
        }
        return token;
    }
}