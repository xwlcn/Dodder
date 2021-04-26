package cc.dodder.torrent.store.config;

import cc.dodder.common.entity.Torrent;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;

@Configuration
public class ElasticsearchConfiguration {

    /**
     * 由于并未使用 ElasticsearchRepository, 并且使用 Client 的方式插入更新索引
     * 导致 Entity 上的 @Mapping 和 @Setting 不会生效，所以自己手动创建索引
     *
     * @param client
     * @param converter
     * @return
     */
    @Bean
    public ElasticsearchRestTemplate elasticsearchRestTemplate(RestHighLevelClient client, ElasticsearchConverter converter) {
        ElasticsearchRestTemplate elasticsearchRestTemplate = new ElasticsearchRestTemplate(client, converter);
        IndexOperations ops = elasticsearchRestTemplate.indexOps(Torrent.class);
        if (!ops.exists()) {
            ops.create();
            ops.putMapping(Torrent.class);
        }
        return elasticsearchRestTemplate;
    }
}