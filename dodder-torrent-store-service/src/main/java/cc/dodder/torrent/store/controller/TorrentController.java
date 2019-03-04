package cc.dodder.torrent.store.controller;

import cc.dodder.api.StoreFeignClient;
import cc.dodder.common.entity.Result;
import cc.dodder.common.entity.Torrent;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.vo.TorrentPageVO;
import cc.dodder.common.vo.TorrentVO;
import cc.dodder.torrent.store.service.TorrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class TorrentController implements StoreFeignClient {

	@Autowired
	private TorrentService torrentService;

	/**
	 * 根据 info_hash 判断数据库是否已经存在
	 *
	 * @param infoHash
	 * @return org.springframework.http.ResponseEntity
	 */
	@GetMapping("/exist/hash/{infoHash}")
	public Result existHash(@PathVariable("infoHash") String infoHash) {
		if (torrentService.existsById(infoHash))
			return Result.noContent();
		return Result.notFount();
	}

	/**
	 * 根据条件搜索 Torrents
	 *
	 * @param request
	 * @return org.springframework.data.domain.Page<cc.dodder.common.entity.Torrent>
	 */
	@PostMapping("/torrents")
	public Result<TorrentPageVO> torrents(@RequestBody SearchRequest request) {
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

	/**
	 * 根据 infoHash 查找 Torrent
	 *
	 * @param infoHash
	 * @return cc.dodder.common.entity.Torrent
	 */
	@RequestMapping("/torrent/{infoHash}")
	@SuppressWarnings("unchecked")
	public Result<TorrentVO> findById(@PathVariable("infoHash") String infoHash) {
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
