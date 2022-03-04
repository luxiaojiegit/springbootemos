package com.example.emos.wx.config.shiro;

import com.example.emos.wx.db.bean.TbUser;

import com.example.emos.wx.db.service.TbUserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OAuth2Realm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TbUserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof OAuth2Token;
    }

    /**
     * 授权（验证权限时使用）
     * @param collection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection collection) {
        //因为认证在前，认证需要用户信息，所有到授权时，可以取出用户信息
        TbUser user = (TbUser) collection.getPrimaryPrincipal();
        //根据id获取改用户的权限
        Set<String> permissions = userService.seachUserPermissions(user.getId());
        System.out.println(permissions);
        //进行授权
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permissions);
        return info;
    }

    /**
     * 认证（登陆时使用）
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //获取token
        String asstoken = (String) token.getPrincipal();
        //根据token获取userid
        int userId = jwtUtil.getUserId(asstoken);
        //根据userid获取用户信息
        TbUser tbUser = userService.searchById(userId);
        //判断该用户是否正常
        if (tbUser ==null){
            throw new LockedAccountException("账户已被锁定，请联系管理员");
        }
        //进行认证
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(tbUser,asstoken,getName());
        return info;
    }
}
