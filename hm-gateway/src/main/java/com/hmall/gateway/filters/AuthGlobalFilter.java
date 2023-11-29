package com.hmall.gateway.filters;

import com.hmall.gateway.JwtTool;
import com.hmall.gateway.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final JwtTool jwtTool;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取请求路径
        ServerHttpRequest request = exchange.getRequest();
        //2.判断是否需要校验
        if (isExclude(request.getPath().toString())) {
            //放行
            return chain.filter(exchange);
        }
        //3.校验解析token
        List<String> headers = request.getHeaders().get("authorization");
        String token = null;
        if (headers != null && !headers.isEmpty()) {
            token = headers.get(0);
        }
        Long userId = null;
        try {
            //内部进行非空判断
            userId = jwtTool.parseToken(token);
        } catch (Exception e) {
            //拦截，设置响应状态码401
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        //5.传递用户信息
        String userInfo = userId.toString();
        ServerWebExchange swe = exchange.mutate()
                .request(builder -> builder.header("user-info", userInfo))
                .build();
        //6.放行
        return chain.filter(swe);
    }

    private boolean isExclude(String path) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (pathMatcher.match(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
