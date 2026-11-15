package demo.springcloud.elasticsearch;

import cn.hutool.json.JSONUtil;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.DecayFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Stats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class ElasticsearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    // 索引操作
    @Test
    void contextLoads() throws IOException {
        // 1、新建索引
        /*
            PUT /索引库名称
            {
              "mappings": {
                "properties": {
                  "字段名":{
                    "type": "text",
                    "analyzer": "ik_smart"
                  },
                  "字段名2":{
                    "type": "keyword",
                    "index": "false"
                  },
                  "字段名3":{
                    "properties": {
                      "子字段": {
                        "type": "keyword"
                      }
                    }
                  },
                  // ...略
                }
              }
            }
         */
        String indexJson = """
                {
                  "mappings": {
                    "properties": {
                      "name":{
                        "type": "text",
                        "analyzer": "ik_smart"
                      },
                      "desc":{
                        "type": "text",
                        "index": "false"
                      }
                    }
                  }
                }
                """;
        // 1.1、创建请求对象
        CreateIndexRequest createIndexRequestrequest = new CreateIndexRequest("test");
        // 1.2、准备请求体
        createIndexRequestrequest.source(indexJson, XContentType.JSON);
        // 1.3、发送请求 RequestOptions.DEFAULT 默认请求头
        client.indices().create(createIndexRequestrequest, RequestOptions.DEFAULT);
        // 2、查询索引
        /*
            GET /索引库名
         */
        GetIndexRequest getRequest = new GetIndexRequest("test");
        boolean flag = client.indices().exists(getRequest, RequestOptions.DEFAULT);
        System.out.println("索引是否存在：" + flag);
        // 3、删除索引
        /*
            DELETE /索引库名称
         */
        DeleteIndexRequest deleteRequest = new DeleteIndexRequest("test");
        client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
    }

    // 文档操作
    @Test
    void documentTest() throws IOException{
        // 1、新增文档
        /*
            POST /{索引库名}/_doc/{id}
            {
                属性: 值,
                属性: 值,
                ...
            }
         */
        demo.springcloud.elasticsearch.po.Test test = new demo.springcloud.elasticsearch.po.Test();
        test.setDesc("test");
        test.setName("test");
        IndexRequest indexRequest = new IndexRequest("test").id("1");
        String json = JSONUtil.toJsonStr(test);
        indexRequest.source(json, XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
        // 2、查询文档
        /*
            GET /{索引库名}/_doc/{id}
         */
        // 2.1.准备Request对象
        GetRequest getRequest = new GetRequest("test").id("1");
        // 2.1.发送请求
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        // 2.3.获取响应结果中的source
        json = response.getSourceAsString();
        test = null;
        test = JSONUtil.toBean(json, demo.springcloud.elasticsearch.po.Test.class);
        System.out.println(test);
        // 3、删除文档
        /*
            DELETE /{索引库名}/_doc/{id}
         */
        DeleteRequest deleteRequest = new DeleteRequest("test").id("1");
        client.delete(deleteRequest, RequestOptions.DEFAULT);
        // 4、修改文档
        /*
            --- 全量修改 ---
            PUT /{索引库名}/_doc/文档id
            {
                "字段1": "值1",
                "字段2": "值2",
                // ... 略
            }
            --- 局部修改 ---
            POST /{索引库名}/_update/文档id
            {
                "doc": {
                     "字段名": "新的值",
                }
            }
         */
        // 在 RestClient 的 API 中，全量修改与新增的 API 完全一致，判断依据是 ID：
        //- 如果新增时，ID 已经存在，则修改
        //- 如果新增时，ID 不存在，则新增
        UpdateRequest updateRequest = new UpdateRequest("test", "1");
        updateRequest.doc(
                "name","TEST",
                "desc","It is a test of elasticsearch"
        );
        client.update(updateRequest, RequestOptions.DEFAULT);
    }

    // 批量操作
    /*
        POST _bulk
        { "index" : { "_index" : "test", "_id" : "1" } }
        { "field1" : "value1" }
        { "delete" : { "_index" : "test", "_id" : "2" } }
        { "create" : { "_index" : "test", "_id" : "3" } }
        { "field1" : "value3" }
        { "update" : {"_id" : "1", "_index" : "test"} }
        { "doc" : {"field2" : "value2"} }
     */
    @Test
    void bulkTest() throws IOException{
        BulkRequest bulkRequest = new BulkRequest();
        demo.springcloud.elasticsearch.po.Test test = new demo.springcloud.elasticsearch.po.Test();
        test.setDesc("another test");
        test.setName("another test");
        bulkRequest.add(
                new IndexRequest("test").id("2").source(JSONUtil.toJsonStr(test), XContentType.JSON),
                new UpdateRequest("test","2").doc("name","Another Test","desc","It is a test of Bulk"),
                new DeleteRequest("test").id("2")
        );
        client.bulk(bulkRequest, RequestOptions.DEFAULT);
    }

    // 叶子查询
    @Test
    void SearchTest() throws IOException{
        // 1、全文检索查询（match/match_all/term/multi_match）
        /*
            GET /{索引库名}/_search
            {
              "query": {
                "match": {
                  "字段名": "搜索条件"
                }
              }
            }
         */
        SearchRequest searchRequest = new SearchRequest("test");
        searchRequest.source().query(
                QueryBuilders.multiMatchQuery("test", "name", "desc")
        );
        client.search(searchRequest, RequestOptions.DEFAULT);
        // 2、复合查询
        /*
            GET /{索引库名}/_search
            {
              "query": {
                "bool": {
                  "must": [
                    { "match": { "字段名": "搜索条件" } }
                  ],
                  "must_not": [
                    { "match": { "字段名": "搜索条件" } }
                  ],
                  "should": [
                    { "match": { "字段名": "搜索条件" } }
                  ],
                  "filter": [
                    { "term": { "字段名": "搜索条件" } }
                  ]
                }
              },
              "sort": [
                { "字段名": { "order": "asc" } }
              ]
            }
         */
        searchRequest = new SearchRequest("test");
        searchRequest.source().query(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("name", "test"))
                        .mustNot(QueryBuilders.matchQuery("desc", "test"))
                        .should(QueryBuilders.matchQuery("name", "test"))
                        .filter(QueryBuilders.termQuery("name", "test"))
        );
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        // 解析查询结果
        SearchHits searchHitshits = searchResponse.getHits();
        // 1.获取总条数
        long total = searchHitshits.getTotalHits().value;
        // 2.遍历结果数组
        SearchHit[] hits = searchHitshits.getHits();
        for (SearchHit hit : hits) {
            // 3.得到_source，也就是原始json文档
            String source = hit.getSourceAsString();
            // 4.反序列化并打印
            demo.springcloud.elasticsearch.po.Test test = JSONUtil.toBean(source, demo.springcloud.elasticsearch.po.Test.class);
            System.out.println(test);
        }
        System.out.println("共搜索到" + total + "条数据");
        // 3、算分操作（权重操作为例）
        /*
            GET /{索引库名}/_search
            {
              "query": {
                "function_score": {
                  "query": {
                    "match": {
                      "字段名": "搜索条件"
                    }
                  },
                  "functions": [
                    {
                      "filter": { // 满足的条件，品牌必须是Iphone
                        "term": {
                          "brand": "Iphone"
                        }
                      },
                      "weight": 10 // 算分权重为2
                    }
                  ],
                  "boost_mode": "multiply" // 加权模式，求乘积
                }
              }
            }
         */
        searchRequest = new SearchRequest("test");
        searchRequest.source().query(
                QueryBuilders.functionScoreQuery(
                        // 原始查询，可以是任意条件
                        QueryBuilders.matchQuery("name", "test"),
                        // 算分函数
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                                new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                        // 满足的条件，品牌必须是Iphone
                                        QueryBuilders.termQuery("brand", "Iphone"),
                                        // 算分权重为2
                                        ScoreFunctionBuilders.weightFactorFunction(10)
                                )
                        }
                        // 加权模式，求合
                        ).boostMode(CombineFunction.SUM)
        ).sort(
                "name", SortOrder.ASC
        ).highlighter(
                SearchSourceBuilder.highlight()
                        .field("name")
                        .preTags("<em>")
                        .postTags("</em>")
        ).size(10).from(0);
        client.search(searchRequest, RequestOptions.DEFAULT);
    }

    // 聚合操作
    /*
        GET /索引库名/_search
        {
          "size": 0,
          "aggs": {
            "聚合名称": {
              "聚合类型": {
                "属性名": "值",
                "size": 20
              }
            }
          }
        }
     */
    @Test
    void AggregrationTest() throws IOException{
        SearchRequest searchRequest = new SearchRequest("test");
        searchRequest.source().query(
                QueryBuilders.matchAllQuery()
        ).size(0).aggregation(
                AggregationBuilders.terms("test_term").field("name").size(10)
                        // 嵌套聚合
                        .subAggregation(
                                AggregationBuilders.stats("test_stats").field("name")
                        )
        );
        // 获取返回结果
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        Aggregations aggregations = response.getAggregations();
        Terms terms = aggregations.get("test_term");
        // 2.获取聚合中的桶
        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        // 3.遍历桶内数据
        for (Terms.Bucket bucket : buckets) {
            // 4.获取桶内key
            String brand = bucket.getKeyAsString();
            System.out.print("brand = " + brand);
            long count = bucket.getDocCount();
            System.out.println("; count = " + count);
            Stats stats = bucket.getAggregations().get("test_stats");
            System.out.println("; stats = " + stats);
        }
    }

}
