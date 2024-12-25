package com.restkeeper.dubbo;


import com.restkeeper.tenant.TenantContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

@Activate //enable plugin of dubbo
@Slf4j
public class DubboConsumerContextFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
       // log.info("shopId-------------------" + RpcContext.getContext().getAttachment("shopId"));
        //log.info("ThreadName---------------" + Thread.currentThread().getName());

        //from custom context obj get token info, save it to rpc context
        RpcContext.getContext().setAttachment("shopId", TenantContext.getShopId());
        String storeId = TenantContext.getStoreId();
        RpcContext.getContext().setAttachment("storeId", storeId);
       // log.info("storeId-------------------" + storeId);
        RpcContext.getContext().setAttachment("loginUserId", TenantContext.getLoginUserId());
        RpcContext.getContext().setAttachment("loginUserName", TenantContext.getLoginUserName());
        return invoker.invoke(invocation);
    }
}
