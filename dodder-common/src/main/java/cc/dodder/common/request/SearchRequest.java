package cc.dodder.common.request;

import lombok.Getter;
import lombok.Setter;

/***
 * 搜索请求
 *
 * @author Mr.Xu
 * @date 2019-03-02 13:52
 **/
@Getter @Setter
public class SearchRequest {

	private String fileName;
	private String fileType;
	private String sortBy;
	private String order = "desc";

	private Integer page;
	private Integer limit = 20;

	public SearchRequest() {
	}
}
