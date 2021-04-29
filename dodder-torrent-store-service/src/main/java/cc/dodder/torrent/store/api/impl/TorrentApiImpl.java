package cc.dodder.torrent.store.api.impl;

import cc.dodder.api.TorrentApi;
import cc.dodder.common.entity.Result;
import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.vo.TorrentPageVO;
import cc.dodder.common.vo.TorrentVO;
import cc.dodder.torrent.store.service.TorrentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Slf4j
@DubboService(version = "${store.service.version}")
public class TorrentApiImpl implements TorrentApi {

	@Autowired
	private TorrentService torrentService;

	@Override
	public Result existHash(String infoHash) {
		if (torrentService.existsById(infoHash))
			return Result.noContent();
		return Result.notFount();
	}

	@Override
	public Result<TorrentPageVO> torrents(SearchRequest request) {
		if (request.getPage() == null || request.getPage() <= 0)
			request.setPage(1);
		Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit());
		Page<Torrent> torrents = torrentService.query(request, pageable);
		Result<TorrentPageVO> result = Result.ok(TorrentPageVO.builder()
				.list(torrents.getContent())
				.total(torrents.getTotalElements())
				.page(request.getPage())
				.limit(request.getLimit())
				.build());
		return result;
	}

	@Override
	public Result<TorrentVO> findById(String infoHash) {
		Optional<Torrent> torrent = torrentService.findById(infoHash);

		Result<TorrentVO> result = Result.ok(TorrentVO.builder()
				.torrent(torrent.orElse(null)).build());
		torrent.ifPresent(t -> {
			Pageable pageable = PageRequest.of(0, 10);
			Page<Torrent> similar = torrentService.findSimilar(t, pageable);
			result.getData().setSimilar(similar.getContent());
		});
		return result;
	}

}
