package cc.dodder.web.controller;

import cc.dodder.api.StoreFeignClient;
import cc.dodder.common.entity.*;
import cc.dodder.common.request.SearchRequest;
import cc.dodder.common.vo.TorrentPageVO;
import cc.dodder.common.vo.TorrentVO;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

@Controller
public class IndexController {

	@Autowired
	private StoreFeignClient storeFeignClient;

	@RequestMapping("/")
	public String index(SearchRequest searchRequest, Model model) {
		if (searchRequest == null)
			searchRequest = new SearchRequest();
		if (searchRequest.getPage() != null && searchRequest.getPage() > 500)
			searchRequest.setPage(500);
		Result<TorrentPageVO> result = storeFeignClient.torrents(searchRequest);
		model.addAttribute("result", result);
		model.addAttribute("searchRequest", searchRequest);
		return "index";
	}

	@RequestMapping("/info/{infoHash}")
	public String info(@PathVariable("infoHash") String infoHash, Model model) {
		if (!infoHash.matches("^[a-zA-Z0-9]{40}$"))
			return "error/404";
		Result<TorrentVO> result = storeFeignClient.findById(infoHash);
		if (result.getData().getTorrent() == null)
			return "error/404";
		Tree tree;
		Torrent torrent = result.getData().getTorrent();
		if ("".equals(torrent.getFiles()) || torrent.getFiles() == null || "null".equals(torrent.getFiles())) {   //单文件
			int pos = torrent.getFileName().lastIndexOf(".");
			String name = pos > 0 ? torrent.getFileName().substring(0, pos) : torrent.getFileName();
			tree = new Tree(name);
			tree.createTree(Arrays.asList(new Node(1, 0, torrent.getFileName(), torrent.getFileSize(), 1)));
		} else {
			tree = JSON.parseObject(torrent.getFiles(), Tree.class);
			tree.getRoot().setFilename(torrent.getFileName());
		}
		model.addAttribute("treeFiles", tree.getHtml(tree.getRoot()));
		model.addAttribute("torrent", torrent);
		model.addAttribute("similar", result.getData().getSimilar());
		return "info";
	}
}
