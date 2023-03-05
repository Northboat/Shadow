import pika
import redis
import subprocess
import requests
import urllib
from queue import Queue
from datetime import datetime
import openai
import os


# redis 连接池
pool = redis.ConnectionPool(host='localhost', port=6379, decode_responses=True, max_connections=4)   # host是redis主机，需要redis服务端和客户端都起着 redis默认端口是6379
# 获取队列名（从 .conf 中）
path = "./shadow/"
name = ""
email = ""
pwd = ""
with open(path+"shadow.conf", 'r') as f:
    for line in f.readlines():
        info = line.strip().split(":")
        tag = info[0].strip()
        content = info[1].strip()
        if(tag == "name"):
            name = content
        elif tag == "email":
            email = content
        elif tag == "password":
            pwd = content

# 命令日志缓存队列，最多可存 6 条交互
q = Queue(14)

def redis_format(str):
    return '\"' + str + '\"'

# 执行命令行
def subprocess_popen(statement):
    p = subprocess.Popen(statement, shell=True, stdout=subprocess.PIPE)
    while p.poll() is None:
        if p.wait() != 0:
            return "命令执行失败"
        else:
            result = ''
            for line in p.stdout.readlines():
                line = line.decode('utf-8').strip()
                line += ' '
                result += line
            return result.strip()


# 聊天机器人
def chat(msg):
    url = 'http://api.qingyunke.com/api.php?key=free&appid=0&msg={}'.format(urllib.parse.quote(msg))
    html = requests.get(url)
    return html.json()["content"]


#openai.api_key = os.environ["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"]
def chat1(msg):
    response = openai.Completion.create(
        engine="davinci",
        prompt=msg,
        max_tokens=2048,
        n=1,
        stop=None,
        temperature=0.7,
    )
    message = response.choices[0].text
    return message


# 通过 redis 回送消息
def send_back(msg):
    # 从池子中拿一个链接
    conn = redis.Redis(connection_pool=pool, decode_responses=True)
    conn.set(name, msg);
    conn.close()

def log(content):
    with open(path+"shadow.log", 'a') as f:
        f.write(content)

def cache(command, result):
    # 当缓存满了，一股脑写入磁盘
    if q.full():
        while q.empty() is False:
            info = q.get()
            log(info + "\n")
    q.put("收到的命令: " + command)
    q.put("返回的处理: " + result)


def get_history():
    i = q.qsize()
    history = ""
    for j in range(0, i):
        history += str(q.queue[j]) + "<br>"
    return history


def login(p):
    #print(pwd)
    #print(p)
    if(pwd == p):
        return "yes"
    return "no"




def shadow():

    # 用于判断是否已登录，防止用户手动键入 login 命令引发错误
    #login = False

    # 统一消息处理函数，执行完成才说明接收完成，此时才可以接收下一条，串行
    def exec(v1, v2, v3, bodyx):
        #global login
        # 将从消息队列接收的字符串格式化
        command = str(bodyx,'utf-8')
        print("收到询问: " + command)
        # 处理命令并获取结果
        if command[0] == '/':
            if command[1:]  == "cache":
                # print("进来了")
                result = get_history()
            elif command[1:].split(" ")[0] == "login":
                result = login(command[1:].split(" ")[1].strip())
            else:
                result = subprocess_popen(command[1:])
        else:
            result = chat(command)
    
        print("处理结果: ", result)
        # 返回结果
        send_back(redis_format(result))
        # 记录缓存
        cache(command, result)

    # mq建立连接
    userx = pika.PlainCredentials("guest","guest")
    conn = pika.BlockingConnection(pika.ConnectionParameters("127.0.0.0",5672,'/',credentials=userx))
    # 开辟管道
    channelx = conn.channel()
    #声明队列，参数为队列名
    channelx.queue_declare(queue = name)


    # 初始化消息队列
    channelx.basic_consume(queue = name, #队列名
                        on_message_callback = exec, #收到消息的回调函数
                        auto_ack = True #是否发送消息确认
                        )

    print("-------- 开始接收数据 -----------")
    # 开始接收消息

    #myDlg.show()

    log("\n" + str(datetime.now().strftime("%Y-%m-%d %H:%M:%S")) + "\n")
    channelx.start_consuming()
