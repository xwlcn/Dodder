package cc.dodder.common.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.io.Serializable;

@Getter @Setter @Builder @ToString
@Document(indexName = "torrent")
@Mapping(mappingPath = "torrent_search_mapping.json")
@Setting(settingPath = "elasticsearch_custom_comma_analyzer.json")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Torrent implements Serializable {

    @Id
    private String infoHash;
    private String fileType = "其他";
    private String fileName;
    //IK分词器对于俄语根本解析不了，单独分离出来
    //其他语言虽然没有专门的分词器准，但至少能够分词，将就着用，如有需要，自行增加额外字段，并且对种子文件名进行语言检测
    private String fileNameRu;
    private long fileSize;

    private long createDate;

    private String files;

    private Integer isXxx = 0;    //is sensitive torrent?

    public Torrent() {
    }

    public Torrent(String infoHash, String fileType, String fileName, String fileNameRu, long fileSize, long createDate, String files, Integer isXXX) {
        this.infoHash = infoHash;
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileNameRu = fileNameRu;
        this.fileSize = fileSize;
        this.createDate = createDate;
        this.files = files;
        this.isXxx = isXXX;
    }

}
