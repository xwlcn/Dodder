<pre>
        ________      _________________
___  __ \___________  /_____  /____________
__  / / /  __ \  __  /_  __  /_  _ \_  ___/
_  /_/ // /_/ / /_/ / / /_/ / /  __/  /
/_____/ \____/\__,_/  \__,_/  \___//_/     一个支持集群部署的分布式 DHT 网络爬虫。
</pre>
-------
# 快速开始
#### 环境依赖
- Zookeeper-3.7.0 ([http://zookeeper.apache.org/](http://zookeeper.apache.org/))
- Kafka-2.13-2.8.0 ([http://kafka.apache.org/](http://kafka.apache.org/))
- Redis-2.6 ([https://redis.io/](https://redis.io/))
- MongoDB-4.4.5 ([https://www.mongodb.com/](https://www.mongodb.com/))
- Elasticsearch-7.12.0 ([https://www.elastic.co/](https://www.elastic.co/))
- elasticsearch-analysis-ik-7.12.0 ([https://github.com/medcl/elasticsearch-analysis-ik](https://github.com/medcl/elasticsearch-analysis-ik))
#### 演示地址
[https://dodder.icu](https://dodder.icu)

announce_peer messages:
![announce_peer](https://github.com/xwlcn/img/raw/master/announce_peer.gif)

单机运行环境：
* CPU:	Intel Xeon E3-1230 v3 - 3.3 GHz - 4 core(s)
* RAM:	32GB - DDR3
* Hard Drive(s):	2x 1TB (HDD SATA)
* Bandwidth:	Unmetered @ 1Gbps

#### 更新日志
* 2021-04-30
  - 优化分词搜索功能
* 2021-04-29
  - 降低 Spring Dubbo 版本（之前的可能导致内存泄露）
  - 移除 KafkaTemplate 的使用，统一使用 Spring Cloud Stream 提供的 StreamBridge
* 2021-04-27
  - 升级 Spring Boot 以及 Spring Cloud 版本
  - 修改 kafka 消费端为批量消费
  - 新增敏感词过滤功能
  - 优化种子下载内存占用一直升高问题
  - 优化 MongoDB 数据存储内容（内存占用是个大问题，以后考虑换 HBase）
  - 合并 MongoDB 入库与 Elasticsearch 索引为同步方法，之前两个不同分组进行入库与索引可能造成先索引数据库中还没有数据的情况，以至于前端网页404问题
* 2019-10-25
  - 升级目前部署在服务器上的 MongoDB，之前 3.6.8 版本频繁挂掉无错误日志
  - 使用 MongoDB 连接池
  - 去掉 indexMessages 主题消息，索引与入库使用 torrentMessages 同一个主题消息（使用不同分组），减少网络传输以及磁盘占用
  - 去除种子信息下载之前的 MongoDB 去重查询（几千个下载线程就是几千个并发查询，过于耗费资源）
* 2019-10-20
  - 优化爬虫速度，新增阻塞线程池用于下载种子信息（降低内存使用）
  - 40万数据时（日爬取35w+新数据）
* 2019-10-13
  - 数据存储到 MongoDB，Elasticsearch 只做索引
  - 升级各个环境依赖的版本
  - 优化数据存储服务，将 Kafka Torrent 的入库和索引消息改为手动提交模式，防止数据丢失
  - 进行两台服务器分布式部署测试，目前成功部署运行中：
     1. A 服务器部署微服务：dht-server、store-service （服务器有限，所以所有环境依赖比如 Elasticsearch 全在这台机器上）
     2. B 服务器部署微服务：dht-server、download-service、dodder-web
* 2019-04-17
  - 初始版本

#### 整体架构
![架构图](https://github.com/xwlcn/Dodder/raw/master/20190305.jpg)

说明：项目中的`dht-server`、`download-service`、`store-service`都是可以集群部署的，
`dht-server`负责爬取 DHT 网络中的 info_hash，然后写入到 Kafka 消息队列中去，`download-service`
负责读取 info_hash 信息到指定 ip 去下载种子文件的 metadata（集群部署时，注意设置好 kafka 主题的分区数量，
分区数量 >= 服务部署个数）。下载好的 metadata 解析出文件信息封装成 Torrent 对象写入 Kafka 的
`torrentMessages`主题中去，`store-service`负责读取 Torrent 存储到 Elasticsearch 中去。

去重：Redis 第一次去重，MongDB 与 Elasticsearch 采用 upsert 插入数据防止重复插入。

#### 部署
前面的环境全部搭好之后，clone 整个项目到本地，如果是集群部署请修改各个服务模块里面的一些 ip 地址参数，
我这里服务器有限，只拿了一台服务器单机部署，集群部署有问题的欢迎提 issue。

### 注意
**dht-server 需要公网 IP 才能爬取到 info_hash**
