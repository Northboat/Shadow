# import pika

# #建立连接
# userx=pika.PlainCredentials("ruroot","rproot")
# conn=pika.BlockingConnection(pika.ConnectionParameters("192.168.153.128",5672,'/',credentials=userx))

# #开辟管道
# channelx=conn.channel()

# #声明队列，参数为队列名
# channelx.queue_declare(queue="shenshupian")

# #发送数据，发送一条，如果要发送多条则复制此段
# channelx.basic_publish(exchange="",
#                        routing_key="shenshupian",# 队列名
#                        body="hello world" # 发送的数据
#                        )
# print("--------发送数据完成-----------")

# #关闭连接
# conn.close()

import pika
import redis
import os

# redis 连接池
# pool = redis.ConnectionPool(host='localhost', port=6379, decode_responses=True)   # host是redis主机，需要redis服务端和客户端都起着 redis默认端口是6379

# mq建立连接
userx = pika.PlainCredentials("guest","guest")
conn = pika.BlockingConnection(pika.ConnectionParameters("127.0.0.0",5672,'/',credentials=userx))

#开辟管道
channelx = conn.channel()

name = "Northboat"

#声明队列，参数为队列名
channelx.queue_declare(queue=name)

def redis_format(str):
    return '\"' + str + '\"'

def cmd(command):
    result = ''
    for line in os.popen(command[1:]).readlines():
        line = line.strip()
        line += ' '
        result += line
    return redis_format(result.strip())

#消息处理函数，执行完成才说明接收完成，此时才可以接收下一条，串行
def exec(v1,v2,v3,bodyx):
    # 字符串格式化
    command=str(bodyx,'utf-8')
    print("得到的命令为:", command)

    # 处理命令
    if command[0] == '/':
        result = cmd(command)
    else:
        result = redis_format(command)

    print("返回的结果为: " + result)
    # 返回结果
    r = redis.Redis(host='localhost', port=6379, decode_responses=True)
    r.set(name, result)     # key是"gender" value是"male" 将键值对存入redis缓存
    r.close()


#接收准备
channelx.basic_consume(queue=name, #队列名
                       on_message_callback=exec, #收到消息的回调函数
                       auto_ack=True #是否发送消息确认
                       )

print("-------- 开始接收数据 -----------")
#开始接收消息
channelx.start_consuming()
