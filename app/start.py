# coding:utf-8

import sys
import window
from PyQt5.QtWidgets import QApplication, QDialog
import pymysql
import subprocess


path = "./shadow/"

class MainDialog(QDialog):
    def __init__(self, parent=None):
        super(QDialog, self).__init__(parent)
        self.ui = window.Ui_Dialog()
        self.ui.setupUi(self)


    def verily(self, name, email):
        conn = pymysql.connect(host = '127.0.0.1' # 连接名称，默认127.0.0.1
            ,user = 'root' # 用户名
            ,passwd='011026' # 密码
            ,port= 3306 # 端口，默认为3306
            ,db='aides' # 数据库名称
            ,charset='utf8' # 字符编码
        )
        cur = conn.cursor() # 生成游标对象
        sql="select * from `user` where `name`= " + '\'' + name + '\'' # SQL语句
        #print(sql)
        cur.execute(sql) # 执行SQL语句
        data = cur.fetchall() # 通过fetchall方法获得数据
        if len(data) == 0:
            print("用户不存在")
            cur.close() # 关闭游标
            conn.close() # 关闭连接
            return False
        if data[0][1] != email:
            print("昵称和邮箱不匹配")
            cur.close() # 关闭游标
            conn.close() # 关闭连接
            return False
        #print("验证成功")
        cur.close() # 关闭游标
        conn.close() # 关闭连接
        return True

    
    def write_conf(self, name, email, pwd):
        with open(path+"shadow.conf", 'w') as f:
            f.write("name: " + name + "\n")
            f.write("email: " + email + "\n")
            f.write("password: " + pwd + "\n")

    def start(self):
        name = self.ui.name.text()
        email = self.ui.email.text()
        pwd = self.ui.pwd.text()
        
        if self.verily(name, email):
            self.write_conf(name, email, pwd)
            # 关闭登陆器
            self.close()
            # 开启监听器
            import shadow
            shadow.shadow()


    
    def clear(self):
        self.ui.name.clear()
        self.ui.email.clear()
        self.ui.pwd.clear()


if __name__ == '__main__':
    myapp = QApplication(sys.argv)
    myDlg = MainDialog()
    myDlg.show()
    sys.exit(myapp.exec_())
