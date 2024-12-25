package com.restkeeper.store.config;


import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.parser.ISqlParserFilter;
import com.baomidou.mybatisplus.core.parser.SqlParserHelper;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantHandler;
import com.baomidou.mybatisplus.extension.plugins.tenant.TenantSqlParser;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.google.common.collect.Lists;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.mybatis.GeneralMetaObjectHandler;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.util.List;

@Slf4j
@Configuration
@AutoConfigureAfter(MybatisPlusTenantConfig.class)
@EnableConfigurationProperties({MybatisPlusProperties.class})
public class MybatisPlusTenantConfig {

    //set multi tenant value
    private static final String SYSTEM_TENANT_SHOPID_COLUMN = "shop_id";
    private static final String SYSTEM_TENANT_STOREID_COLUMN = "store_id";

    //table ignore tenant operation
    private static final List<String> IGNORE_TENANT_TABLES  = Lists.newArrayList("");

    @Bean
    public PaginationInterceptor paginationInterceptor(){
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();

        TenantSqlParser tenantSqlParser_shop = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                //get from rpc content
                String shopId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_SHOPID);

                if (shopId == null) {
                    throw new RuntimeException("get shopId error");
                }
                return new StringValue(shopId);
            }

            @Override
            public String getTenantIdColumn() {
                return SYSTEM_TENANT_SHOPID_COLUMN;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return IGNORE_TENANT_TABLES.stream().anyMatch((e)->e.equalsIgnoreCase(tableName));
            }
        });


        TenantSqlParser tenantSqlParser_store = new TenantSqlParser().setTenantHandler(new TenantHandler() {
            @Override
            public Expression getTenantId(boolean where) {
                //get from rpc content
                String storeId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_STOREID);
                log.info("Fetched storeId in getTenantId: " + storeId);
                if (storeId == null) {
                    throw new RuntimeException("get storeId error");
                }
                return new StringValue(storeId);
            }

            @Override
            public String getTenantIdColumn() {
                return SYSTEM_TENANT_STOREID_COLUMN;
            }

            @Override
            public boolean doTableFilter(String tableName) {
                return IGNORE_TENANT_TABLES.stream().anyMatch((e)->e.equalsIgnoreCase(tableName));
            }
        });

        paginationInterceptor.setSqlParserList(Lists.newArrayList(tenantSqlParser_shop, tenantSqlParser_store));

        paginationInterceptor.setSqlParserFilter(new ISqlParserFilter() {
            @Override
            public boolean doFilter(MetaObject metaObject) {
                MappedStatement ms = SqlParserHelper.getMappedStatement(metaObject);
                //在 MyBatis 中，SQL ID 是每个 SQL 映射（MappedStatement）的唯一标识符，它由 Mapper 接口的类名和方法名组成
                //{mapper类的全限定名}.{方法名}
                if("com.restkeeper.store.mapper.StaffMapper.login".equals(ms.getId())){
                    return true;
                }
                return false;
            }
        });

        return paginationInterceptor;
    }
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();

        return druidDataSource;
    }

    @Primary//@Primary标识必须配置在代码数据源上，否则本地事务失效
    @Bean("dataSource")
    public DataSourceProxy dataSourceProxy(DataSource druidDataSource) {
        return new DataSourceProxy(druidDataSource);
    }

    private MybatisPlusProperties properties;
    public MybatisPlusTenantConfig(MybatisPlusProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MybatisSqlSessionFactoryBean sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception {

        // 这里必须用 MybatisSqlSessionFactoryBean 代替了 SqlSessionFactoryBean，否则 MyBatisPlus 不会生效
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(dataSourceProxy);
        mybatisSqlSessionFactoryBean.setTransactionFactory(new SpringManagedTransactionFactory());

        GlobalConfig globalConfig  = new GlobalConfig();
        globalConfig.setMetaObjectHandler(new GeneralMetaObjectHandler());
        mybatisSqlSessionFactoryBean.setGlobalConfig(globalConfig);
        mybatisSqlSessionFactoryBean.setPlugins(paginationInterceptor());

        mybatisSqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath*:/mapper/*.xml"));

        MybatisConfiguration configuration = this.properties.getConfiguration();
        if(configuration == null){
            configuration = new MybatisConfiguration();
        }
        mybatisSqlSessionFactoryBean.setConfiguration(configuration);

        return mybatisSqlSessionFactoryBean;
    }

}
