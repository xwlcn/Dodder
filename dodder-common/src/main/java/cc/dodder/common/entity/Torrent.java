package cc.dodder.common.entity;


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
@Document(indexName = "dodder", type = "torrent")
@Mapping(mappingPath = "torrent_search_mapping.json")
@Setting(settingPath = "elasticsearch_custom_comma_analyzer.json")
public class Torrent implements Serializable {

    @Id
    private String infoHash;
    private String fileType = "其他";
    private String fileName;

    private long fileSize;

    private long createDate;

    private String files;

    public Torrent() {
    }

    public Torrent(String infoHash, String fileType, String fileName, long fileSize, long createDate, String files) {
        this.infoHash = infoHash;
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.createDate = createDate;
        this.files = files;
    }

}
