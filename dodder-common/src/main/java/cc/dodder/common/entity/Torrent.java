package cc.dodder.common.entity;

import lombok.Data;

import java.util.List;
import java.util.Date;

/***
 * 种子文件信息
 *
 * @author Mr.Xu
 * @since 2019-02-22 14:21
 **/
@Data
public class Torrent {
	private String infoHash;
	private String announce;
	private List<String> announceList;
	private Date creationDate;
	private String comment;
	private String createdBy;
	private String type;

	private Info info;
}