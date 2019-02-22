package cc.dodder.common.entity;

import lombok.Data;

/***
 * 多文件种子文件信息
 *
 * @author Mr.Xu
 * @since 2019-02-22 14:57
 **/
@Data
public class SubFile {
	private long length;
	private String path;

	public SubFile(long length, String path) {
		this.length = length;
		this.path = path;
	}
}
