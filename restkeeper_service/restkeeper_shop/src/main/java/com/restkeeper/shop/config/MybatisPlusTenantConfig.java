package com.restkeeper.shop.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;

import com.google.common.collect.Lists;
import com.restkeeper.constants.SystemCode;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MybatisPlusTenantConfig {

    //define current Multi-tenancy flag
    private static final String SYSTEM_TENANT_ID_COLUMN = "shop_id";

    //exclude table without Multi-tenancy operations
    private static final List<String> IGNORE_TENANT_TABLES = Lists.newArrayList("");

    /*当你使用 MyBatis Plus 执行查询时，PaginationInterceptor 会拦截 SQL 查询，检查是否有分页参数，
    如果有，则会自动在 SQL 中添加 LIMIT 和 OFFSET 语句，以便数据库返回指定页的数据。
    拦截器的核心任务是为查询语句添加分页逻辑，同时保证查询结果的正确性和高效性。*/
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        System.out.println("----------------paginationInterceptor starts----------------");
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        /*TenantSqlParser 通过拦截并修改 SQL 语句，在 SELECT、UPDATE 和 DELETE 等操作中自动添加租户条件，
        确保多租户系统中不同租户之间的数据互相隔离。这种机制确保即使不同租户访问同一个数据库，他们也只能访问自己的数据。*/
        TenantSqlParser tenantSqlParser = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            /*指定一个 租户处理器（Tenant Handler），用于处理与多租户相关的逻辑。*/

            @Override
            public Expression getTenantId(boolean where) {
                //String shopId = "test";
                String shopId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_SHOPID);

                if (shopId == null) {
                    throw new RuntimeException("Get tenantId error");
                }
                return new StringValue(shopId);
            }

            //set column mapped to the id
            @Override
            public String getTenantIdColumn() {
                return SYSTEM_TENANT_ID_COLUMN;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                /*anyMatch((e) -> e.equalsIgnoreCase(tableName)) 是流操作中的一种匹配操作，
                它会检查流中的元素是否有任意一个满足给定的条件。
	        •	(e) -> e.equalsIgnoreCase(tableName) 是一个 Lambda 表达式，表示一个条件判断。对于列表 IGNORE_TENANT_TABLES 中的每个元素 e，判断它是否与传入的 tableName 相同（忽略大小写）。
	        •	equalsIgnoreCase 方法用于比较字符串，忽略大小写。*/
                return IGNORE_TENANT_TABLES.stream().anyMatch((e)->e.equalsIgnoreCase(tableName));
            }
        });
        paginationInterceptor.setSqlParserList(Lists.newArrayList(tenantSqlParser));



        return paginationInterceptor;
    }

}
