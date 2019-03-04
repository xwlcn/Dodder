package cc.dodder.torrent.store.service;

import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.entity.Torrent;
import cc.dodder.torrent.store.repository.TorrentRepository;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.sort.SortBuilders.fieldSort;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.elasticsearch.search.sort.SortOrder.DESC;

@Service
public class TorrentService {

	@Autowired
	private TorrentRepository torrentRepository;

	public boolean existsById(String infoHash) {
		return torrentRepository.existsById(infoHash);
	}

	public void upsert(Torrent torrent) throws IOException {
		torrentRepository.upsert(torrent);
	}

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
		} else {
			query.withSort(fieldSort("createDate").order(DESC));
		}

		query.withPageable(pageable).withQuery(boolQuery);
		Page<Torrent> page = torrentRepository.search(query.build());
		return page;
	}

	public Optional<Torrent> findById(String infoHash) {
		return torrentRepository.findById(infoHash);
	}

	public Page<Torrent> findSimilar(Torrent torrent, Pageable pageable) {
		return torrentRepository.searchSimilar(torrent, new String[] {"fileName"}, pageable);
	}
}
