---
title: 基于 RabbitMQ 的私人电脑助手
date: 2022-2-24
tags:
  - Web
  - Java
categories:
  - WebApp
---

## 前期准备

### CRUD

环境搭建

```bash
yay -S rabbitmq rabbitmqadmin
```

springboot 配置

```yaml
spring:
  application:
    name: Aside
  datasource:
    username: root
    password: "011026"
    url: jdbc:mysql://localhost:3306/aides?useUnicode=true&characterEncoding=utf-8
    driver-class-name: com.mysql.cj.jdbc.Driver
  thymeleaf:
    cache: false
  data:
    redis:
      host: localhost
      port: 6379
      password: "011026"
  mail:
    host: smtp.qq.com
    username: "northboat@qq.com"
    password: "cckrfidlqckzeabb"
    default-encoding: UTF-8


#整合mybatis
mybatis:
  type-aliases-package: com.northboat.aides.pojo
  # classpath 指 resources 目录
  mapper-locations: classpath:/mapper/*.xml

server:
  port: 8080
```

依赖

```xml
<dependencies>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!-- Thymeleaf -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- MySQL-->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
    <!-- lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <!-- test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- rabbit mq -->
    <dependency>
        <groupId>org.springframework.amqp</groupId>
        <artifactId>spring-rabbit-test</artifactId>
        <scope>test</scope>
    </dependency>
    <!-- mybatis -->
    <dependency>
        <groupId>org.mybatis.spring.boot</groupId>
        <artifactId>mybatis-spring-boot-starter</artifactId>
        <version>3.0.1</version>
    </dependency>
    <!--mail-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <!--WebSocket-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
</dependencies>
```

报错

```bash
Unsatisfied dependency expressed through field 'userMapper'
```

检查 scan mapper 是否路径写错

```java
@MapperScan("com.northboat.aides.mapper")
```

帐号登录，邮件发放，WebSocket 服务器与上一个项目基本保持一致，不多赘述

### RabbitMQ API

下载 pika

- Python-RabbitMQ API

```bash
yay -S python-pip
pip3 install pika
```

简单模型测试：一对一

Java-RabbitMQ 发布消息

```java
public class RabbitMQUtil {
    public static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        // 创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 工厂IP，连接RabbitMQ的队列
        factory.setHost("127.0.0.1");
        // 用户名
        factory.setUsername("guest");
        // 密码
        factory.setPassword("guest");
        // 创捷连接
        Connection connection = factory.newConnection();
        // 创建信道
        Channel channel = connection.createChannel();
        /**
         * 生成一个队列
         * 1、队列名称
         * 2、队列里面的消息是否持久化，默认情况消息存储再内存中，设置true保存磁盘中
         * 3、该队列是否只供一个消费者消费，true可以多个消息者消费,false只能一个
         * 4、是否自动删除，ture自动删除，false不自动删除
         * 5、其他参数
         */
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        // 发消息
        String message = "hahaha";
        /**
         * 发送一个消费
         * 1、发送到那个交换机
         * 2、路由的key值是那个 ，本次是队列的名称
         * 3、其他参数
         * 4、发送消息的消息体
         */
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        channel.close();
        connection.close();
    }
}
```

Python pika 消费消息

```python
import pika

#建立连接
userx = pika.PlainCredentials("guest","guest")
conn = pika.BlockingConnection(pika.ConnectionParameters("127.0.0.0",5672,'/',credentials=userx))

#开辟管道
channelx = conn.channel()

name = "hello"

#声明队列，参数为队列名
channelx.queue_declare(queue=name)

#消息处理函数，执行完成才说明接收完成，此时才可以接收下一条，串行
def dongcallbackfun(v1,v2,v3,bodyx):
    print("得到的数据为:",bodyx)

#接收准备
channelx.basic_consume(queue=name, #队列名
                       on_message_callback=dongcallbackfun, #收到消息的回调函数
                       auto_ack=True #是否发送消息确认
                       )
print("-------- 开始接收数据 -----------")

#开始接收消息
channelx.start_consuming()
```

- 这里的 start_consuming 是一个死循环体，会持续监听消息队列
- dongcallbackfun 为回调函数，操作空间极大
