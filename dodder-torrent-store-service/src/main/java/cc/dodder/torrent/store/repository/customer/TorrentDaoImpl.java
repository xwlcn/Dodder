package cc.dodder.torrent.store.repository.customer;

import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
public class TorrentDaoImpl implements TorrentDao {

	@Autowired
	private ElasticsearchTemplate template;

	@Override
	public void upsert(Torrent torrent) throws IOException {
		Client client = template.getClient();

		XContentBuilder source = jsonBuilder()
				.startObject()
				.field("fileName", torrent.getFileName())
				.field("fileType", torrent.getFileType())
				.field("fileSize", torrent.getFileSize())
				.field("createDate", torrent.getCreateDate())
				.field("files", torrent.getFiles())
				.endObject();

		IndexRequest indexRequest = new IndexRequest("dodder", "torrent", torrent.getInfoHash())
				.source(source);
		UpdateRequest updateRequest = new UpdateRequest("dodder", "torrent", torrent.getInfoHash())
				.doc(source)
				.upsert(indexRequest);
		client.update(updateRequest);
	}

	@Override
	public Page<Torrent> query(SearchRequest request, Pageable pageable) {
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
		Page<Torrent> page = template.queryForPage(query.build(), Torrent.class, new SearchResultMapper() {

			@Override
			public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {

				long totalHits = response.getHits().getTotalHits();
				float maxScore = response.getHits().getMaxScore();

				List<Torrent> results = new ArrayList<>();
				for (SearchHit hit : response.getHits().getHits()) {
					if (hit == null)
						continue;
					Torrent result;
					result = JSON.parseObject(hit.getSourceAsString(), Torrent.class);
					result.setInfoHash(hit.getId());
					if (hit.getHighlightFields().containsKey("fileName"))
						result.setFileName(hit.getHighlightFields().get("fileName").fragments()[0].toString());
					else
						result.setFileName((String) hit.getSourceAsMap().get("fileName"));
					results.add(result);
				}
				return new AggregatedPageImpl<>((List<T>) results, pageable, totalHits, response.getAggregations(), response.getScrollId(),
							maxScore);
			}
		});
		return page;
	}


}
