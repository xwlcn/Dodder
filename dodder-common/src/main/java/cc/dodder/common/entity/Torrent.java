package cc.dodder.common.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.Mapping;

import java.io.Serializable;

@Getter @Setter @Builder @ToString
@Document(indexName = "dodder", type = "torrent")
@Mapping(mappingPath = "torrent_search_mapping.json")
public class Torrent implements Serializable {

    @Id
    private String infoHash;
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private String fileType = "其他";
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private String fileName;
    @Field(index = false)
    private long fileSize;
    @Field
    private long createDate;
    @Field(index = false)
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
