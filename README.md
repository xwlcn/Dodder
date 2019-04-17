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
- Nacos-0.8.0 ([http://nacos.io](http://nacos.io))
- Zookeeper-3.4.12 ([http://zookeeper.apache.org/](http://zookeeper.apache.org/))
- Kafka-2.11-2.1.0 ([http://kafka.apache.org/](http://kafka.apache.org/))
- Elasticsearch-6.6.0 ([https://www.elastic.co/](https://www.elastic.co/))
- elasticsearch-analysis-ik-6.6.0 ([https://github.com/medcl/elasticsearch-analysis-ik](https://github.com/medcl/elasticsearch-analysis-ik))
#### 演示地址
[https://dodder.cc](https://dodder.cc)

单机运行环境：
* CPU:	Intel Xeon E3-1230 v3 - 3.3 GHz - 4 core(s)
* RAM:	32GB - DDR3
* Hard Drive(s):	2x 1TB (HDD SATA)
* Bandwidth:	Unmetered @ 1Gbps

#### 整体架构
![架构图](https://github.com/xwlcn/Dodder/raw/master/20190305.jpg)

说明：项目中的`dht-server`、`download-service`、`store-service`都是可以集群部署的，
`dht-server`负责爬取 DHT 网络中的 info_hash，然后写入到 Kafka 消息队列中去，`download-service`
负责读取 info_hash 信息到指定 ip 去下载种子文件的 metadata（集群部署时，注意设置好 kafka 主题的分区数量，
分区数量 >= 服务部署个数）。下载好的 metadata 解析出文件信息封装成 Torrent 对象写入 Kafka 的
`torrentMessages`主题中去，`store-service`负责读取 Torrent 存储到 Elasticsearch 中去。

去重：`dht-server`中使用`Redis`第一次进行拦截过滤，`download-service`查询`Elasticsearch`进行
二次判断去重，`store-service`中采用`upsert`进行第三次去重。实际上`upsert`已经完全可以进行去重了，
前面两次是用来减少下载次数，提升爬取的速度。

#### 部署
前面的环境全部搭好之后，clone 整个项目到本地，如果是集群部署请修改各个服务模块里面的一些 ip 地址参数，
我这里服务器有限，只拿了一台服务器单机部署，集群部署有问题的欢迎提 issue。
