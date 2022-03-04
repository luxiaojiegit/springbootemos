package com.example.emos.wx.config.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.Map;


@Configuration
public class ShiroConfig {
    /**
     * 用于封装Reaml对象
     * @param oAuth2Realm
     * @return
     */

    @Bean
    public SecurityManager securityManager(OAuth2Realm oAuth2Realm){
        //创建安全管理器
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        //给安全管理器设置自定义reaml
        defaultWebSecurityManager.setRealm(oAuth2Realm);
        defaultWebSecurityManager.setRememberMeManager(null);
        return defaultWebSecurityManager;
    }

    /**
     * 用于封装Filter对象，设置Filter拦截路径
     * @param securityManager
     * @param oAuth2Filter
     * @return
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,OAuth2Filter oAuth2Filter){
        ShiroFilterFactoryBean shiroFilter  = new ShiroFilterFactoryBean();
        //给filter设置安全管理器
        shiroFilter.setSecurityManager(securityManager);

        //设置过滤
        Map<String, Filter> filters = new LinkedHashMap();
        filters.put("oauth2", oAuth2Filter);
        shiroFilter.setFilters(filters);

        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/webjars/**", "anon");
        filterMap.put("/druid/**", "anon");
        filterMap.put("/app/**", "anon");
        filterMap.put("/sys/login", "anon");
        filterMap.put("/swagger/**", "anon");
        filterMap.put("/v2/api-docs", "anon");
        filterMap.put("/swagger-ui.html", "anon");
        filterMap.put("/swagger-resources/**", "anon");
        filterMap.put("/captcha.jpg", "anon");
        filterMap.put("/user/register", "anon");
        filterMap.put("/user/login", "anon");
//        filterMap.put("/test/**", "anon");
        //所有请求都需要oauth2进行认证
        filterMap.put("/**", "oauth2");
        filterMap.put("/meeting/recieveNotify", "anon");
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        return shiroFilter;
    }

    /**
     * 设置shiro生命周期
     * @return
     */
    @Bean
    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
        return new LifecycleBeanPostProcessor();

    }

    /**
     * 设置aop切面类，web方法执行前，验证权限，匹配所有类，匹配所有加了认证注解的方法
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor advisor(SecurityManager securityManager){
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
    }
}
