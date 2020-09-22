# APM-应用服务性能监控

APM: application perfomance monotor

APM agent->APM server->ElasticSearch->kibana



## 安装elastic

### 1、解压包

tar -xvzf elasticsearch-7.9.0.tar.gz

tar -zxvf kibana-7.9.0.tar.gz

注意：elastic依赖jdk，这个版本的es依赖jdk14

### 2、配置

换一个账号elk，对elk授权，设置用户组组，不能用root

cd elasticsearch-7.9.0/config

里面有个elasticsearch.yml，需要配置

```yml
network.host:192.168.86.101 #绑定ip

Discovery.send_hosts:["192.168.86.10:9300"]

Cluster.initial_maste_node:["192.168.86.10:9300"]
```







### 3、启动

sh elasticsearch-7.9.0/bin/elasticsearch 

如果启动失败了重新配置，再启动时要删除elasticsearch/data下面的数据

sh elasticsearch-7.9.0/bin/elasticsearch -d 后台启动



4、验证

Elasticsearch-head 里面连接



## 安装kibana

### 1、配置

kibana/config/kibana.yml

```yml

server.port:5601

server.host:"0.0.0.0"

elasticsearch.hoast:["http://192.168.86.10:9200"]
```



### 2、启动

sh kibana/bin/kibana

### 3、访问

http://192.168.86:5601

第一次会问是否需要加载模拟数据



## 安装APMserver

### 1、解压

 ### 2、配置

配置文件：apm-server.yml

```yml
apm-server：

  host："192.168.86.101:8200"

开启监控。。。。

Output.elasticsearch

   host:   #配置es地址
```



### 3、启动

sh apm-server

可以通过kibana的页面看启动情况



## 在spring cloud里面设置

依赖：

<groupId>Org.elasticsearch.client

<artifactId>Elasticsearch-rest-high-level-client



<artifactId>apm-agent-api

使用：

RestHignLevelClient client = new RestHignLevelClient(....设置连接es)



ElasticApm.startTransaction();

启动服务时要设置：

-Djavaagent:../../../elastic-apm-agent-1.18.0.jar(agent jar包的路径)

-Delastic.apm.service_name=设置自己的服务名

-Delastic.apm.server_urls=设置apm-server的地址

-Delastic.apm.service_token=

-Delastic.apm.application_package=com.nil 设置要监控的包





## linux命令巩固：

nestate  -ntplu

ifconfig：查看网络iplinux命令巩固：

















