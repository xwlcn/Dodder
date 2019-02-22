package cc.dodder.common.entity;

import lombok.Data;

import java.util.List;

/***
 * 种子文件 Info 信息
 *
 * @author Mr.Xu
 * @since 2019-02-22 14:55
 **/
@Data
public class Info {
	private String name;
	private Long length;
	private Long pieceLength;
	private byte[] pieces;

	private List<SubFile> files;
}
