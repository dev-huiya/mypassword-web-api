package me.huiya.core.Config;

import me.huiya.core.Common.JWTManager;
import me.huiya.core.Repository.TokenRepository;
import me.huiya.core.Repository.UserRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.beans.ConstructorProperties;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static JwtAuthInterceptor JwtAuthInterceptor;

    @ConstructorProperties({
        "JwtAuthInterceptor",
    })
    public WebConfig(
        JwtAuthInterceptor JwtAuthInterceptor
    ) {
        this.JwtAuthInterceptor = JwtAuthInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .maxAge(3000);
    }

    private static final String[] INTERCEPTOR_WHITE_LIST = {
        "/error",
        // /error 를 화이트리스트로 등록하지 않으면 서버 에러시 /error 호출하면서 인터셉터가 다시 실행된다
        // 2020-12-25 18:28 hw.kim

        // 인터셉터 제외 처리를 어노테이션으로 분리함.
        // 참고 : https://krespo.net/192
        // 2021-07-15 13:34 Hawon Kim
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(JwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(INTERCEPTOR_WHITE_LIST);
    }
}