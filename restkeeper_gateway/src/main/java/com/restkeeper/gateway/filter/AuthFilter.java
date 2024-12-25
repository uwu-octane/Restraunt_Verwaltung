package com.restkeeper.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.restkeeper.utils.JWTUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RefreshScope
public class AuthFilter implements GlobalFilter, Ordered {

    //"#{...}"：这是 Spring 表达式语言（SpEL）的语法，用于动态解析属性的值。
    @Value("#{'{$gateway.excludeUrls}'.split(',')}")
    private List<String> excludeUrls;

    @Value("${gateway.secret")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //get response object
        ServerHttpResponse response = exchange.getResponse();

        //get request path
        String path = exchange.getRequest().getURI().getPath();


        if (excludeUrls.contains(path)){
            return chain.filter(exchange);
        }

        //get token info
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (StringUtils.isNotEmpty(token)) {
            //valid
            JWTUtil.VerifyResult verifyResult = JWTUtil.verifyJwt(token, secret);
            if (verifyResult.isValidate()){
                return chain.filter(exchange);
            } else {
                Map<String, Object> responseData = Maps.newHashMap();
                responseData.put("code", verifyResult.getCode());
                responseData.put("message", "valid failed");
                return  responseError(response, responseData);
            }
        }else {
            Map<String, Object> responseData = Maps.newHashMap();
            responseData.put("code", 401);
            responseData.put("message", "invalid request");
            responseData.put("cause", "token is empty");
            return  responseError(response, responseData);
        }

    }


    private Mono<Void> responseError(ServerHttpResponse response, Map<String, Object> responseData) {
        //to json
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = new byte[0];

        try{
            data = objectMapper.writeValueAsBytes((responseData));
        }catch (Exception e){
            e.printStackTrace();
        }

        DataBuffer buffer = response.bufferFactory().wrap(data);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=utf-8");
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
