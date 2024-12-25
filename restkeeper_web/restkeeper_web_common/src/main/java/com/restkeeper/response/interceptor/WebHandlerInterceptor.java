package com.restkeeper.response.interceptor;

import com.restkeeper.tenant.TenantContext;
import com.restkeeper.utils.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@Component
public class WebHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //get jwt
        String tokenInfo = request.getHeader("Authorization");

        if (StringUtils.isNotEmpty(tokenInfo)){
            try{
                //decode token
                Map<String, Object> tokenMap = JWTUtil.decode(tokenInfo);
                //String shopId = (String) tokenMap.get("shopId");

                //set to RPCContext
                //RpcContext.getContext().setAttachment("shopId", shopId);

                //put map to custom context obj
                TenantContext.addAttachments(tokenMap);

            }catch (Exception e){
                log.error("decode token failed");
                e.printStackTrace();
            }
        }
        return true;
    }
}
