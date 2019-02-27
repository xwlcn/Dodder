package cc.dodder.common.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;

@Getter @Setter @Builder @ToString
@Document(indexName = "dodder", type = "torrent")
public class Torrent implements Serializable {

    @Id
    private String infoHash;
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private String fileType;
    @Field(index = false)
    private long filesize;
    @Field
    private long createDate;
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private String files;

    public Torrent() {
    }

    public Torrent(String infoHash, String fileType, long filesize, long createDate, String files) {
        this.infoHash = infoHash;
        this.fileType = fileType;
        this.filesize = filesize;
        this.createDate = createDate;
        this.files = files;
    }
}
