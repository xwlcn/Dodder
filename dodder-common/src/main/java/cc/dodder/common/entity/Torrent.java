package cc.dodder.common.entity;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.io.Serializable;

@Getter @Setter @Builder
@Document(indexName = "dodder", type = "torrent")
public class Torrent implements Serializable {

    @Id
    private String infoHash;
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private String fileType;
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private long createDate;
    @Field(searchAnalyzer = "ik_max_word",analyzer = "ik_smart")
    private String files;

}
