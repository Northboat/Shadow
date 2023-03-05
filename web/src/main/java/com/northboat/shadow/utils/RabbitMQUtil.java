package com.northboat.shadow.utils;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Component
public class RabbitMQUtil {
    public static final String QUEUE_NAME = "hello";

    // 创建一个连接工厂
    private static final ConnectionFactory factory = new ConnectionFactory();

    static {
        // 工厂IP，连接RabbitMQ的队列
        factory.setHost("127.0.0.1");
        // 用户名
        factory.setUsername("guest");
        // 密码
        factory.setPassword("guest");
        factory.setHost("127.0.0.1");
        factory.setUsername("guest");
        factory.setPassword("guest");
    }

    public boolean send (String queueName, String command) {
        try {
            // 创捷连接
            Connection connection = factory.newConnection();
            // 创建一个信道
            Channel channel = connection.createChannel();
            /**
             * 生成一个队列
             * 1、队列名称
             * 2、队列里面的消息是否持久化，默认情况消息存储再内存中，设置true保存磁盘中
             * 3、该队列是否只供一个消费者消费，true可以多个消息者消费,false只能一个
             * 4、是否自动删除，ture自动删除，false不自动删除
             * 5、其他参数
             */
            channel.queueDeclare(queueName, false, false, false, null);
            /**
             * 发送一个消费
             * 1、发送到那个交换机
             * 2、路由的key值是那个 ，本次是队列的名称
             * 3、其他参数
             * 4、发送消息的消息体
             */
            channel.basicPublish("", queueName, null, command.getBytes());
            channel.close();
            connection.close();
            return true;
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clear(String name){
        try{
            // 创捷连接
            Connection connection = factory.newConnection();
            // 创建一个信道
            Channel channel = connection.createChannel();
            channel.queuePurge(name);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
