package cc.dodder.torrent.store.config;

import cc.dodder.common.entity.Torrent;
import org.elasticsearch.client.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
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
    public ElasticsearchTemplate elasticsearchTemplate(Client client, ElasticsearchConverter converter) {
        try {
            ElasticsearchTemplate elasticsearchTemplate = new ElasticsearchTemplate(client, converter);
            //手动创建索引
            elasticsearchTemplate.createIndex(Torrent.class);
            elasticsearchTemplate.putMapping(Torrent.class);
            return elasticsearchTemplate;
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
