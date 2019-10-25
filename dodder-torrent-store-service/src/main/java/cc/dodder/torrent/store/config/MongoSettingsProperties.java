package cc.dodder.torrent.store.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@PropertySource(value = "classpath:mongo-pool.properties")
@ConfigurationProperties(prefix = "spring.data.mongodb")
public class MongoSettingsProperties {

    private List<String> address;
    private String replicaSet;
    private String database;
    private String username;
    private String password;
    private Integer minConnectionsPerHost = 0;
    private Integer maxConnectionsPerHost = 100;
    private Integer threadsAllowedToBlockForConnectionMultiplier = 5;
    private Integer serverSelectionTimeout = 30000;
    private Integer maxWaitTime = 120000;
    private Integer maxConnectionIdleTime = 0;
    private Integer maxConnectionLifeTime = 0;
    private Integer connectTimeout = 10000;
    private Integer socketTimeout = 0;
    private Boolean socketKeepAlive = false;
    private Boolean sslEnabled = false;
    private Boolean sslInvalidHostNameAllowed = false;
    private Boolean alwaysUseMBeans = false;
    private Integer heartbeatConnectTimeout = 20000;
    private Integer heartbeatSocketTimeout = 20000;
    private Integer minHeartbeatFrequency = 500;
    private Integer heartbeatFrequency = 10000;
    private Integer localThreshold = 15;
    private String authenticationDatabase;


}