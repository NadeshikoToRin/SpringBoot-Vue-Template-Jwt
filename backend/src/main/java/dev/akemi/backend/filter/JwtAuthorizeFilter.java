package dev.akemi.backend.filter;

// 引入JWT解码相关的类
import com.auth0.jwt.interfaces.DecodedJWT;
// 引入自定义的JWT工具类
import dev.akemi.backend.utils.JwtUtils;

// 引入必要的Servlet类
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 引入Spring Security的相关类
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthorizeFilter 类用于过滤HTTP请求并进行JWT验证
 * 继承自OncePerRequestFilter，确保每个请求只被处理一次
 */
@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {

    // 注入JwtUtils工具类
    @Resource
    JwtUtils utils;

    /**
     * 处理过滤器的主要逻辑
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain  过滤器链
     * @throws ServletException  Servlet异常
     * @throws IOException  IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中获取Authorization信息
        String authorization = request.getHeader("Authorization");

        // 解析并验证JWT
        DecodedJWT jwt = utils.resolveJwt(authorization);

        // 如果JWT有效，则进行用户身份认证
        if (jwt != null) {
            // 根据JWT创建UserDetails对象
            UserDetails userDetails = utils.toUserDetails(jwt);
            // 创建身份验证对象，包含用户信息和权限
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            // 设置请求的详细信息
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // 将身份认证信息存入SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 将用户ID存入请求属性中，以供后续使用
            request.setAttribute("id", utils.toId(jwt));
        }

        // 继续执行过滤器链中的下一个过滤器
        filterChain.doFilter(request, response);
    }
}
