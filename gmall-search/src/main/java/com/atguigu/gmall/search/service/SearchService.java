package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.ParsedAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @program: gmall
 * @description:
 * @author: lgd
 * @create: 2021-01-29 23:07
 **/
@Service
public class SearchService {


    @Autowired
    RestHighLevelClient restHighLevelClient;


    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildSearchSourceBuilder(searchParamVo));

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchResponseVo searchResponseVo= parseResult(searchResponse);
            searchResponseVo.setPageNum(searchParamVo.getPageNum());
            searchResponseVo.setPageSize(searchParamVo.getPageSize());
            return searchResponseVo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //解析查询结果
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo searchResponseVo=new SearchResponseVo();

        SearchHits hits = response.getHits();
        List<Goods> goodsList = Stream.of(hits.getHits()).map(hit->{
            String source = hit.getSourceAsString();
            Goods goods = JSON.parseObject(source,Goods.class);
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Text[] fragments = title.getFragments();
            goods.setTitle(fragments[0].string());
            return goods;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);

        searchResponseVo.setTotal(hits.totalHits);


        Aggregations aggregations = response.getAggregations();

        Map<String, Aggregation> stringAggregationMap = aggregations.asMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)stringAggregationMap.get("brandIdAgg");
        ParsedNested attrAgg = (ParsedNested)stringAggregationMap.get("attrAgg");
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)stringAggregationMap.get("categoryIdAgg");

        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();

        if(!CollectionUtils.isEmpty(brandBuckets)){
            List<BrandEntity> brandEntities = brandBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(bucket.getKeyAsNumber().longValue());
                Map<String, Aggregation> brandNameAggMap = bucket.getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandNameAggMap.get("brandNameAgg");
                ParsedStringTerms LogoAgg = (ParsedStringTerms) brandNameAggMap.get("brandLogoAgg");
                List<? extends Terms.Bucket> buckets = brandNameAgg.getBuckets();
                List<? extends Terms.Bucket> logobuckets = LogoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(buckets)) {
                    brandEntity.setName(buckets.get(0).getKeyAsString());
                }
                if (!CollectionUtils.isEmpty(logobuckets)) {
                    brandEntity.setLogo(logobuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            searchResponseVo.setBrands(brandEntities);
        }
        if(!CollectionUtils.isEmpty(categoryIdAggBuckets)){
            List<CategoryEntity> categoryEntities=categoryIdAggBuckets.stream().map(  bucket->{
                    CategoryEntity categoryEntity=new CategoryEntity();
                  categoryEntity.setId(bucket.getKeyAsNumber().longValue());
              ParsedStringTerms categoryNameAgg= bucket.getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> buckets = categoryNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(buckets)){
                        categoryEntity.setName(buckets.get(0).getKeyAsString());
                    }
                return categoryEntity;
                   }
            ).collect(Collectors.toList());
            searchResponseVo.setCategories(categoryEntities);
        }
        if(!CollectionUtils.isEmpty(attrIdBuckets)){
            List<SearchResponseAttrVo> searchResponseAttrVos=attrIdBuckets.stream().map(bucket->{
                SearchResponseAttrVo searchResponseAttrVo=new SearchResponseAttrVo();
                searchResponseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());

                Map<String, Aggregation> subAggMap = bucket.getAggregations().asMap();
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)subAggMap.get("attrNameAgg");
                List<? extends Terms.Bucket> attrNamebuckets = attrNameAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrNamebuckets)){
                    searchResponseAttrVo.setAttrName(attrNamebuckets.get(0).getKeyAsString());
                }

                ParsedStringTerms attrValueAgg = (ParsedStringTerms)subAggMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueBuckets = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(attrValueBuckets)){

                    List<String> attrValues = attrValueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());

                    searchResponseAttrVo.setAttrValues(attrValues);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setFilters(searchResponseAttrVos);
        }

        return searchResponseVo;
    }


    //构建查询条件
    private SearchSourceBuilder buildSearchSourceBuilder(SearchParamVo searchParamVo)  {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


        String keyword = searchParamVo.getKeyword();
        List<Long> brandId=searchParamVo.getBrandId();
        //关键字
        if (StringUtils.isBlank(keyword)&&CollectionUtils.isEmpty(brandId)){
            //关键字为空,直接返回
            return searchSourceBuilder;
        }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //品牌
        List<Long> brandIds = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandIds)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIds));
        }
        //分类
        List<Long> categoryId = searchParamVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //价格区间
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQueryBuilder.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQueryBuilder.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }
        //是否有货
        Boolean store = searchParamVo.getStore();
        if (store!=null&&store) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        //属性检索
        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> { //4:8G-12G
                String[] attr = StringUtils.split(prop, ":");
                if (attr != null && attr.length == 2) {
                    //取出attrId
                    BoolQueryBuilder boolQueryBuilder1 = QueryBuilders.boolQuery();
                    boolQueryBuilder1.must(QueryBuilders.termQuery("searchAttrs.attrId", attr[0]));

                    //取出attrvalues(多选)
                    String[] attrValues = StringUtils.split(attr[1], "-");
                    boolQueryBuilder1.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQueryBuilder1, ScoreMode.None));

                }


            });
        }
        //排序
        Integer sort = searchParamVo.getSort();
        if(sort!=null){
            switch (sort){
                case 1:
                    searchSourceBuilder.sort("price", SortOrder.DESC);  break;
                case 2:
                    searchSourceBuilder.sort("price", SortOrder.ASC); break;
                case 3:
                    searchSourceBuilder.sort("sales", SortOrder.DESC); break;
                case 4:
                    searchSourceBuilder.sort("createTime", SortOrder.DESC);break;
                default:
                    searchSourceBuilder.sort("_score", SortOrder.DESC);break;
            }
        }

        //分页
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        searchSourceBuilder.from((pageNum-1)*pageSize);
        searchSourceBuilder.size(pageSize);
        //高亮
        searchSourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font color='red'>")
                .postTags("</font>")
        );
        //品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg")
                .field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("brandLogoAgg").field("logo")));
        //分类聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //规格参数聚合
        searchSourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg","searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))
                )
        );
        //结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"skuId","defaultImage","title","subTitle","price"},null);

        SearchSourceBuilder query = searchSourceBuilder.query(boolQueryBuilder);

        return query;
    }

}



