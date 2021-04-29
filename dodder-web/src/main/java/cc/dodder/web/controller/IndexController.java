package cc.dodder.web.controller;

import cc.dodder.api.TorrentApi;
import cc.dodder.common.entity.Result;
import cc.dodder.common.entity.Torrent;
import cc.dodder.common.entity.Tree;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.util.JSONUtil;
import cc.dodder.common.util.StringUtil;
import cc.dodder.common.vo.TorrentPageVO;
import cc.dodder.common.vo.TorrentVO;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

	@DubboReference(timeout = 30000, version = "${store.service.version}", url = "${store.service.url}")
	private TorrentApi torrentApi;

	private static final String TREE_HTML = "<ul class=\"filetree treeview\"><p><span class=\"bticon\">#1</span></p><li class=\"last\"><span class=\"file\">#2<small>#3</small></span></li></ul>";

	@RequestMapping("/")
	public String index(SearchRequest searchRequest, Model model) {
		if (searchRequest == null)
			searchRequest = new SearchRequest();
		if (searchRequest.getPage() != null && searchRequest.getPage() > 500)
			searchRequest.setPage(500);
		Result<TorrentPageVO> result = torrentApi.torrents(searchRequest);
		model.addAttribute("result", result);
		model.addAttribute("searchRequest", searchRequest);
		return "index";
	}

	@RequestMapping("/info/{infoHash}")
	public String info(@PathVariable("infoHash") String infoHash, Model model) {
		if (!infoHash.matches("^[a-zA-Z0-9]{40}$"))
			return "error/404";
		Result<TorrentVO> result = torrentApi.findById(infoHash);
		if (result.getData().getTorrent() == null)
			return "error/404";
		Torrent torrent = result.getData().getTorrent();
		if ("".equals(torrent.getFiles()) || torrent.getFiles() == null || "null".equals(torrent.getFiles())) {   //单文件
			int pos = torrent.getFileName().lastIndexOf(".");
			String name = pos > 0 ? torrent.getFileName().substring(0, pos) : torrent.getFileName();
			model.addAttribute("treeFiles", TREE_HTML.replace("#1", name)
					.replace("#2", torrent.getFileName())
					.replace("#3", StringUtil.formatSize(torrent.getFileSize())));
		} else {
			Tree tree = JSONUtil.parseObject(torrent.getFiles(), Tree.class);
			tree.getRoot().setFilename(torrent.getFileName());
			model.addAttribute("treeFiles", tree.getHtml(tree.getRoot()));
		}
		model.addAttribute("torrent", torrent);
		model.addAttribute("similar", result.getData().getSimilar());
		return "info";
	}
}
