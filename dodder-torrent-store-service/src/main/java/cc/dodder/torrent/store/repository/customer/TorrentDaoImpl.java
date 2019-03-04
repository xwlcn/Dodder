package cc.dodder.torrent.store.repository.customer;

import cc.dodder.common.entity.Torrent;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import java.io.IOException;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/***
 * 自定义扩展 Torrent Dao 实现类
 *
 * @author Mr.Xu
 * @date 2019-02-25 11:09
 **/
public class TorrentDaoImpl implements TorrentDao {

	@Autowired
	private ElasticsearchTemplate template;

	@Override
	public void upsert(Torrent torrent) throws IOException {
		Client client = template.getClient();

		XContentBuilder source = jsonBuilder()
				.startObject()
				.field("fileName", torrent.getFileName())
				.field("fileType", torrent.getFileType())
				.field("fileSize", torrent.getFileSize())
				.field("createDate", torrent.getCreateDate())
				.field("files", torrent.getFiles())
				.endObject();

		IndexRequest indexRequest = new IndexRequest("dodder", "torrent", torrent.getInfoHash())
				.source(source);
		UpdateRequest updateRequest = new UpdateRequest("dodder", "torrent", torrent.getInfoHash())
				.doc(source)
				.upsert(indexRequest);
		client.update(updateRequest);
	}
}
