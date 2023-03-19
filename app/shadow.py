import pika
import redis
import subprocess
import requests
import urllib
from queue import Queue
from datetime import datetime
import openai


# redis 连接池
pool = redis.ConnectionPool(host='43.163.218.127', port=6379, decode_responses=True, max_connections=4, password='123456')   # host是redis主机，需要redis服务端和客户端都起着 redis默认端口是6379
# 获取队列名（从 .conf 中）
path = "./"
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

# 命令日志缓存队列，最多可存 7 条交互
q = Queue(14)

def redis_format(str):
    return f'\"{str}\"'



# 命令行
p = subprocess.Popen("/bin/bash", shell=True, stdin=subprocess.PIPE, stderr=subprocess.PIPE, stdout=subprocess.PIPE)
def stdout():
    global p
    while True:
        setback(p.stdout.readline().decode('utf8'))

def stderr():
    global p
    while True:
        setback(p.stderr.readline().decode('utf8'))

cmd_back = f""

def setback(str):
    global cmd_back
    global back_finished
    cmd_back += str.strip()+"\n"

def getback():
    global cmd_back
    global back_finished
    result = cmd_back
    cmd_back = f""
    return result.strip()

import threading
out = threading.Thread(target=stdout)
out.daemon = True
err = threading.Thread(target=stderr)
err.daemon = True
out.start()
err.start()

import os
import time
# 执行命令行
def cmd(statement):
    global p   
    global back_finished
    statement += os.linesep
    p.stdin.write(statement.encode('utf8'))
    p.stdin.flush()
    time.sleep(0.5)
    return getback().replace("\n", "<br>")







# 聊天机器人
def chat1(msg):
    url = 'http://api.qingyunke.com/api.php?key=free&appid=0&msg={}'.format(urllib.parse.quote(msg))
    html = requests.get(url)
    return html.json()["content"]

# chatGPT API
openai.api_key = "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"
def chat2(msg):
    messages = []
    messages.append({"role":"system","content":""})
    messages.append({"role":"user","content": msg})
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=messages
    )
    reply = response["choices"][0]["message"]["content"]
    return reply

# 本地模型
params = {
    'max_new_tokens': 200,
    'do_sample': True,
    'temperature': 0.5,
    'top_p': 0.9,
    'typical_p': 1,
    'repetition_penalty': 1.05,
    'top_k': 0,
    'min_length': 0,
    'no_repeat_ngram_size': 0,
    'num_beams': 1,
    'penalty_alpha': 0,
    'length_penalty': 1,
    'early_stopping': False,
}
# Server address
server = "127.0.0.1"
def chat(msg):
    #print(msg)
    response = requests.post(f"http://{server}:7860/run/textgen", json={
        "data": [
            msg,
            params['max_new_tokens'],
            params['do_sample'],
            params['temperature'],
            params['top_p'],
            params['typical_p'],
            params['repetition_penalty'],
            params['top_k'],
            params['min_length'],
            params['no_repeat_ngram_size'],
            params['num_beams'],
            params['penalty_alpha'],
            params['length_penalty'],
            params['early_stopping'],
        ]
    }).json()
    data = response['data'][0]
    print(data)
    reply = data.split("\n")
    return reply[0] + reply[1]



# 通过 redis 回送消息
def send_back(msg):
    # 从池子中拿一个链接
    conn = redis.Redis(connection_pool=pool, decode_responses=True)
    conn.rpush(name, msg)
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
        history += str(q.queue[j])
    return history


def login(p):
    if(pwd == p):
        return f"yes"
    return f"no"




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
                result = get_history()
            elif command[1:].split(" ")[0] == "login":
                result = login(command[1:].split(" ")[1].strip())
            else:
                result = cmd(command[1:])
        else:
            result = chat(command).strip().replace("\n", "<br>")
    
        print("处理结果: " + result)
        # 返回结果
        send_back(redis_format(result))
        # 记录缓存
        cache(command, result)

    # mq建立连接
    userx = pika.PlainCredentials("admin","011026")
    conn = pika.BlockingConnection(pika.ConnectionParameters("43.163.218.127",5672,'/',credentials=userx))
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
