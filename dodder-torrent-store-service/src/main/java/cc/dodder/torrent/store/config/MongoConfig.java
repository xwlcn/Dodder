package cc.dodder.torrent.store.config;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {
    // 覆盖默认的MongoDbFactory
    @Bean
    MongoDbFactory mongoDbFactory(MongoSettingsProperties mongoSettingsProperties) {
        // 客户端配置（连接数、副本集群验证）
        MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
        builder.connectionsPerHost(mongoSettingsProperties.getMaxConnectionsPerHost());
        builder.minConnectionsPerHost(mongoSettingsProperties.getMinConnectionsPerHost());
        if (mongoSettingsProperties.getReplicaSet() != null) {
            builder.requiredReplicaSetName(mongoSettingsProperties.getReplicaSet());
        }
        builder.threadsAllowedToBlockForConnectionMultiplier(
                mongoSettingsProperties.getThreadsAllowedToBlockForConnectionMultiplier());
        builder.serverSelectionTimeout(mongoSettingsProperties.getServerSelectionTimeout());
        builder.maxWaitTime(mongoSettingsProperties.getMaxWaitTime());
        builder.maxConnectionIdleTime(mongoSettingsProperties.getMaxConnectionIdleTime());
        builder.maxConnectionLifeTime(mongoSettingsProperties.getMaxConnectionLifeTime());
        builder.connectTimeout(mongoSettingsProperties.getConnectTimeout());
        builder.socketTimeout(mongoSettingsProperties.getSocketTimeout());
        builder.sslEnabled(mongoSettingsProperties.getSslEnabled());
        builder.sslInvalidHostNameAllowed(mongoSettingsProperties.getSslInvalidHostNameAllowed());
        builder.alwaysUseMBeans(mongoSettingsProperties.getAlwaysUseMBeans());
        builder.heartbeatFrequency(mongoSettingsProperties.getHeartbeatFrequency());
        builder.minHeartbeatFrequency(mongoSettingsProperties.getMinHeartbeatFrequency());
        builder.heartbeatConnectTimeout(mongoSettingsProperties.getHeartbeatConnectTimeout());
        builder.heartbeatSocketTimeout(mongoSettingsProperties.getHeartbeatSocketTimeout());
        builder.localThreshold(mongoSettingsProperties.getLocalThreshold());

        MongoClientOptions mongoClientOptions = builder.build();

        // MongoDB地址列表
        List<ServerAddress> serverAddresses = new ArrayList<>();
        for (String address : mongoSettingsProperties.getAddress()) {
            String[] hostAndPort = address.split(":");
            String host = hostAndPort[0];
            Integer port = Integer.parseInt(hostAndPort[1]);
            ServerAddress serverAddress = new ServerAddress(host, port);
            serverAddresses.add(serverAddress);
        }

        // 连接认证
        if (mongoSettingsProperties.getUsername() == null) {
            throw new RuntimeException("mongodb username can not be null");
        }

        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(
                mongoSettingsProperties.getUsername(),
                mongoSettingsProperties.getAuthenticationDatabase() != null ? mongoSettingsProperties
                        .getAuthenticationDatabase() : mongoSettingsProperties.getDatabase(),
                mongoSettingsProperties.getPassword().toCharArray());
        // 创建客户端和Factory
        MongoClient mongoClient = new MongoClient(serverAddresses, mongoCredential, mongoClientOptions);
        MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(mongoClient,mongoSettingsProperties.getDatabase());

        return mongoDbFactory;
    }
}