package me.huiya.core.Config;

import me.huiya.core.Common.JWTManager;
import me.huiya.core.Exception.AuthRequiredException;
import me.huiya.core.Exception.ManagerAuthorityRequiredException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_TOKEN_KEY = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // 컨트롤러에 @WithOutAuth 어노테이션이 사용되었는지 체크함
        WithOutAuth withoutAuth = ((HandlerMethod) handler).getMethodAnnotation(WithOutAuth.class);
        ManagerAuthority managerAuthority = ((HandlerMethod) handler).getMethodAnnotation(ManagerAuthority.class);

        if(withoutAuth == null || managerAuthority != null) {
            // @WithOutAuth 없으면 인증 체크
            String token = request.getHeader("Authorization");
            if(token == null) {
                throw new AuthRequiredException("Required token");
            }
            token = token.replace(HEADER_TOKEN_KEY, "");
            JWTManager.verify(token);

            // @ManagerAuthority 어노테이션이 있으면 토큰에 isManager가 true 체크
            if(managerAuthority != null) {
                HashMap<String, Object> info = JWTManager.read(token);
                Boolean isManager = (Boolean) info.get("isManager");

                if(isManager == null || isManager != true) {
                    throw new ManagerAuthorityRequiredException("Required manager Authority");
                }
            }
        } else {
            // @WithOutAuth 어노테이션이 있으므로 인증 체크하지 않고 넘어감
            // 2021-07-15 13:55 Hawon Kim
        }

        return true;
    }
}