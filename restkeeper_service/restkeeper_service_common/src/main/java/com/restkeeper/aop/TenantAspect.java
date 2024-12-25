package com.restkeeper.aop;

import com.restkeeper.constants.SystemCode;
import com.restkeeper.tenant.TenantContext;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantAspect {

    @Pointcut("@annotation(com.restkeeper.aop.TenantAnnotation)")
    public void tenantAnnotation(){

    }

    @Before("tenantAnnotation()")
    public void doBeforeAdvice(JoinPoint joinPoint){
        TenantContext.addAttachment(SystemCode.TENANT_CONDITION_SHOPID,
                RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_SHOPID));
        TenantContext.addAttachment(SystemCode.TENANT_CONDITION_STOREID,
                RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_STOREID));
    }

    @After("tenantAnnotation()")
    public void doAfterAdvice(JoinPoint joinPoint){
        TenantContext.clear();
    }
}
