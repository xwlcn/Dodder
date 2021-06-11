package cc.dodder.torrent.store.repository;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.util.LanguageUtil;
import cc.dodder.torrent.store.TorrentStoreServiceApplication;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.elasticsearch.index.query.QueryBuilders.*;
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
            if (torrent.getFileName() != null)
                map.put("fileName", torrent.getFileName());
            if (torrent.getFileNameRu() != null) {
                map.put("fileNameRu", torrent.getFileNameRu());
            }
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
            String fileName = torrent.getFileName();
            if (fileName == null)
                fileName = torrent.getFileNameRu();
            Update update = new Update().set("infoHash", torrent.getInfoHash())
                    .set("createDate", torrent.getCreateDate())
                    .set("fileName", fileName)
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
        NativeSearchQueryBuilder query = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolean ruLang = false;

        if (request.getFileType() != null) {
            boolQuery.filter(matchQuery("fileType", request.getFileType()));
        }
        if (TorrentStoreServiceApplication.filterXxx) {
            boolQuery.filter(termQuery("isXxx", 0));
        }

        if (request.getFileName() != null) {
            if ("ru".equals(LanguageUtil.getLanguage(request.getFileName()))) {
                ruLang = true;
            }
            query.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
            if (ruLang)
                boolQuery.must(matchQuery("fileNameRu", request.getFileName()).minimumShouldMatch("3<60%"));
            else
                boolQuery.must(matchQuery("fileName", request.getFileName()).minimumShouldMatch("3<60%"));
        } else {
            boolQuery.filter(matchAllQuery());
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
                .withQuery(boolQuery);
        if (request.getFileName() != null) {
            if (ruLang) {
                query.withHighlightFields(new HighlightBuilder.Field("fileNameRu"));
            } else {
                query.withHighlightFields(new HighlightBuilder.Field("fileName"));
            }
        }

        SearchHits<Torrent> searchHits = elasticsearchRestTemplate.search(query.build(), Torrent.class);
        if (request.getFileName() != null) {
            for (SearchHit<Torrent> hit : searchHits) {
                if (hit == null) continue;
                Torrent torrent = hit.getContent();
                if (hit.getHighlightFields().containsKey("fileName"))
                    torrent.setFileName(hit.getHighlightField("fileName").get(0));
                if (hit.getHighlightFields().containsKey("fileNameRu"))
                    torrent.setFileName(hit.getHighlightField("fileNameRu").get(0));
                if (torrent.getFileName() == null)
                    torrent.setFileName(torrent.getFileNameRu());
            }
        }

        AggregatedPage<SearchHit<Torrent>> page = new AggregatedPageImpl<>(
                searchHits.getSearchHits(),
                pageable,
                searchHits.getTotalHits(),
                searchHits.getAggregations(),
                null,
                searchHits.getMaxScore());
        return (Page<Torrent>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public Page<Torrent> searchSimilar(Torrent torrent, Pageable pageable) {
        String field = "fileName";
        if ("ru".equals(LanguageUtil.getLanguage(torrent.getFileName())))
            field = "fileNameRu";
        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = QueryBuilders.moreLikeThisQuery(new String[]{field},
                new String[] {torrent.getFileName()},
                new MoreLikeThisQueryBuilder.Item[] {new MoreLikeThisQueryBuilder.Item("torrent", torrent.getInfoHash())});
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (TorrentStoreServiceApplication.filterXxx) {
            boolQuery.must(termQuery("isXxx", 0));
        }
        moreLikeThisQueryBuilder.minTermFreq(1).maxQueryTerms(15);
        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withPageable(pageable)
                .withQuery(boolQuery.must(moreLikeThisQueryBuilder)).build();

        SearchHits<Torrent> searchHits = elasticsearchRestTemplate.search(query, Torrent.class);
        searchHits.stream().filter(hit -> hit.getContent().getFileNameRu() != null).forEach(hit -> hit.getContent().setFileName(hit.getContent().getFileNameRu()));
        AggregatedPage<SearchHit<Torrent>> page = SearchHitSupport.page(searchHits, pageable);

        return (Page<Torrent>) SearchHitSupport.unwrapSearchHits(page);
    }

    @Override
    public boolean existsById(String infoHash) {
        return mongoTemplate.exists(Query.query(Criteria.where("infoHash").is(infoHash)), Torrent.class);
    }

    @Override
    public Long countAll() {
        return mongoTemplate.estimatedCount(Torrent.class);
    }

}