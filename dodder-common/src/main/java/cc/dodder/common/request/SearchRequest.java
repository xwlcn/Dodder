package cc.dodder.common.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/***
 * 搜索请求
 *
 * @author Mr.Xu
 * @date 2019-03-02 13:52
 **/
@Getter @Setter
public class SearchRequest implements Serializable {

	private String fileName;
	private String fileType;
	private String sortBy;
	private String order;

	private Integer page;
	private Integer limit = 20;

	public SearchRequest() {
	}
}
