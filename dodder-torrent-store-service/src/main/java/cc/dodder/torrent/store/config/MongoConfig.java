package cc.dodder.torrent.store.config;

import cc.dodder.common.entity.Torrent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;

@Configuration
@ImportResource("classpath:mongodb.xml")
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDatabaseFactory);
        if (!mongoTemplate.collectionExists(Torrent.class)) {
            mongoTemplate.createCollection(Torrent.class);
            mongoTemplate.indexOps(Torrent.class).ensureIndex(new Index().on("isXxx", Sort.Direction.ASC));
        }
        return mongoTemplate;
    }

}