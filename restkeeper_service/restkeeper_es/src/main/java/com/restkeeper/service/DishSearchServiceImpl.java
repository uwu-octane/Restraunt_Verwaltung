package com.restkeeper.service;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.Token;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.entity.DishEs;
import com.restkeeper.entity.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.Service;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.nlpcn.es4sql.domain.Where;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;
import org.nlpcn.es4sql.parse.SqlParser;
import org.nlpcn.es4sql.parse.WhereParser;
import org.nlpcn.es4sql.query.maker.QueryMaker;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service(version = "1.0.0",protocol = "dubbo")
public class DishSearchServiceImpl implements IDishSearchService{

    @Value("${es.host}")
    private String host;

    @Value("${es.port}")
    private int port;

    @Override
    public SearchResult<DishEs> searchAllByCode(String code, int type, int pageNum, int pageSize) {

        String shopId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_SHOPID);
        String storeId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_STOREID);
        if (StringUtils.isEmpty(shopId)){
            throw new RuntimeException("shop id not exist");
        }

        if (StringUtils.isEmpty(storeId)){
            throw new RuntimeException("storeId id not exist");
        }


        return this.queryIndexContent("dish","code like '%"+code+"%' and type = '"+type+"' and is_deleted=0 and shop_id = '"+shopId+"' and store_id= '"+storeId+"' order by last_update_time desc",pageNum,pageSize);
    }

    private SearchResult<DishEs> queryIndexContent(String indexName, String condition, int pageNum, int pageSize) {

        //创建一个 RestHighLevelClient 实例，连接到指定的 Elasticsearch 节点（由 host 和 port 确定）。
        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(host,port,"http")));

        SearchRequest request = new SearchRequest(indexName);
        //用于构建搜索请求的主体内容。它允许您配置查询的各种参数，例如分页、排序、查询条件、过滤器等。
        // SearchSourceBuilder 的实例通常被用在 SearchRequest 对象中，用于定义具体的搜索细节
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int start = (pageNum - 1) * pageSize;
        searchSourceBuilder.from(start);
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.trackTotalHits(true);

        //condition
        BoolQueryBuilder boolQueryBuilder = this.creatQueryBuilder(indexName,condition);
        //query(QueryBuilder query)：设置查询条件。可以将各种查询（如 match、term、bool 等）通过 QueryBuilder 添加到搜索请求中。
        searchSourceBuilder.query(boolQueryBuilder);

        SearchResponse searchResponse = null;

        try {
            searchResponse =  client.search(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        List<DishEs> listData = Lists.newArrayList();
        for (SearchHit hit : searchHits) {
            Map<String, Object> data = hit.getSourceAsMap();
            String jsonMap = JSON.toJSONString(data);
            DishEs dishEs = JSON.parseObject(jsonMap, DishEs.class);
            listData.add(dishEs);
        }

        //close client link
        try {
            client.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        SearchResult<DishEs> result = new SearchResult<>();
        result.setRecords(listData);
        result.setTotal(hits.getTotalHits().value);
        return result;

    }

    private BoolQueryBuilder creatQueryBuilder(String indexName, String condition) {
        BoolQueryBuilder boolQueryBuilder = null;

        try {
            SqlParser sqlParser = new SqlParser();
            String sql = "select * from " + indexName;
            String whereTemp = "";
            if (!Strings.isNullOrEmpty(condition)){
                whereTemp = " where 1=1 and " + condition;
            }

            SQLQueryExpr sqlQueryExpr = (SQLQueryExpr)this.toSqlExpr(sql+whereTemp);
            MySqlSelectQueryBlock query = (MySqlSelectQueryBlock) sqlQueryExpr.getSubQuery().getQuery();
            WhereParser whereParser = new WhereParser(sqlParser,query);
            Where where = whereParser.findWhere();
            if (where != null){
                boolQueryBuilder = QueryMaker.explan(where);
            }
        } catch (SqlParseException e) {
            log.error(e.getMessage());
        }


        return boolQueryBuilder;
    }

    private SQLExpr toSqlExpr(String sql) {
        SQLExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF){
            throw new ParseException("Sql not valid: " + sql);
        }
        return expr;
    }


    @Override
    public SearchResult<DishEs> searchCuisineByCode(String code, int pageNum, int pageSize) {
        String shopId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_SHOPID);
        String storeId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_STOREID);
        if (StringUtils.isEmpty(shopId)){
            throw new RuntimeException("shop id not exist");
        }

        if (StringUtils.isEmpty(storeId)){
            throw new RuntimeException("storeId id not exist");
        }

        return this.queryIndexContent("dish","code like '%"+code+"%' and is_deleted=0 and shop_id ='"+shopId+"' and store_id ='"+storeId+"' order by last_update_time desc", pageNum, pageSize);
    }

    @Override
    public SearchResult<DishEs> searchCuisineByName(String name, int type, int pageNum, int pageSize) {
        String shopId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_SHOPID);
        String storeId = RpcContext.getContext().getAttachment(SystemCode.TENANT_CONDITION_STOREID);
        if (StringUtils.isEmpty(shopId)){
            throw new RuntimeException("shop id not exist");
        }

        if (StringUtils.isEmpty(storeId)){
            throw new RuntimeException("storeId id not exist");
        }

        return queryIndexContent("dish","dish_name like '%"+name+"%' and type="+type+" and is_deleted=0 and shop_id ='"+shopId+"' and store_id ='"+storeId+"' order by last_update_time desc",
                pageNum,
                pageSize);
    }
}
