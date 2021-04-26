package cc.dodder.torrent.store.repository;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.util.JSONUtil;
import cc.dodder.torrent.store.TorrentStoreServiceApplication;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.MoreLikeThisQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.elasticsearch.support.SearchHitsUtil;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;

/***
 * 自定义扩展 Torrent Dao 实现类
 *
 * @author Mr.Xu
 * @date 2019-02-25 11:09
 **/
@Repository
public class TorrentDaoImpl implements TorrentDao {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void index(List<Torrent> torrents) {
        List<UpdateQuery> list = new ArrayList<>();
        for (Torrent torrent: torrents) {

            Map<String, Object> map = new HashMap<>();
            map.put("fileName", torrent.getFileName());
            map.put("createDate", torrent.getCreateDate());
            map.put("fileSize", torrent.getFileSize());
            map.put("fileType", torrent.getFileType());
            map.put("isXxx", torrent.getIsXxx());
            Document document = Document.from(map);
            document.setId(torrent.getInfoHash());

            UpdateQuery updateQuery = UpdateQuery.builder(torrent.getInfoHash()).withDocument(document).withDocAsUpsert(true).build();
            list.add(updateQuery);
        }
        elasticsearchRestTemplate.bulkUpdate(list, Torrent.class);
    }

    @Override
    public void upsert(List<Torrent> torrents) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, Torrent.class);
        for (Torrent torrent: torrents) {
            Query query = Query.query(Criteria.where("infoHash").is(torrent.getInfoHash()));
            Update update = new Update().set("infoHash", torrent.getInfoHash())
                    .set("createDate", torrent.getCreateDate())
                    .set("fileName", torrent.getFileName())
                    .set("files", torrent.getFiles())
                    .set("fileSize", torrent.getFileSize())
                    .set("fileType", torrent.getFileType())
                    .set("isXxx", torrent.getIsXxx());
            ops.upsert(query, update);
        }
        ops.execute();
    }

    @Override
    public Optional<Torrent> findById(String id) {
        return Optional.ofNullable(mongoTemplate.findById(id, Torrent.class));
    }

    @Override
    public Page<Torrent> query(SearchRequest request, Pageable pageable) {
        long now = System.currentTimeMillis();
        NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (request.getFileName() != null) {
            boolQuery.must(matchQuery("fileName", request.getFileName()));
        } else {
            boolQuery.must(matchAllQuery());
        }
        if (request.getFileType() != null) {
            boolQuery.must(matchQuery("fileType", request.getFileType()));
        }
        if (TorrentStoreServiceApplication.filterXxx) {
            boolQuery.must(matchQuery("isXxx", 0));
        }
        if (request.getSortBy() != null) {
            if (ASC.toString().equals(request.getOrder()))
                query.withSort(fieldSort("createDate").order(ASC));
            else
                query.withSort(fieldSort("createDate").order(DESC));
        } else if (StringUtils.isEmpty(request.getFileName())) {
            query.withSort(fieldSort("createDate").order(DESC));
        }

        query.withPageable(pageable)
                .withQuery(boolQuery)
                .withHighlightFields(new HighlightBuilder.Field("fileName"));

        SearchHits<Torrent> searchHits = elasticsearchRestTemplate.search(query.build(), Torrent.class);
        for (SearchHit<Torrent> hit: searchHits) {
            if (hit == null) continue;
            Torrent torrent = hit.getContent();
            if (hit.getHighlightFields().containsKey("fileName"))
                torrent.setFileName(hit.getHighlightField("fileName").get(0));
        }

        //fix totalElements
        AggregatedPage<SearchHit<Torrent>> page = new AggregatedPageImpl<>(
                searchHits.getSearchHits(),
                pageable,
                countAll(),
                searchHits.getAggregations(),
                null,
                searchHits.getMaxScore());
        System.out.println("====================================" + (System.currentTimeMillis() - now));
        return (Page<Torrent>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Page<Torrent> searchSimilar(Torrent torrent, String[] fields, Pageable pageable) {
        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = QueryBuilders.moreLikeThisQuery(fields,
                new String[] {torrent.getFileName()},
                new MoreLikeThisQueryBuilder.Item[] {new MoreLikeThisQueryBuilder.Item("torrent", torrent.getInfoHash())});
        moreLikeThisQueryBuilder.minTermFreq(1);
        SearchHits<Torrent> searchHits = elasticsearchRestTemplate.search(new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(moreLikeThisQueryBuilder).build(), Torrent.class);
        AggregatedPage<SearchHit<Torrent>> page = SearchHitSupport.page(searchHits, pageable);
        return (Page<Torrent>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public boolean existsById(String infoHash) {
        return mongoTemplate.exists(Query.query(Criteria.where("infoHash").is(infoHash)), Torrent.class);
    }

    @Override
    public Long countAll() {
        return mongoTemplate.count(new Query(), Torrent.class);
    }

}