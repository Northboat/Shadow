# -*- coding: utf-8 -*-

# Form implementation generated from reading ui file 'Weather.ui'
#
# Created by: PyQt5 UI code generator 5.13.2
#
# WARNING! All changes made in this file will be lost!


from PyQt5 import QtCore, QtGui, QtWidgets

path = "./shadow/"
def read_conf():
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
        return [name, email, pwd]

class Ui_Dialog(object):
    def setupUi(self, Dialog):
        Dialog.setObjectName("Dialog")
        Dialog.resize(400, 300)

        self.appBox = QtWidgets.QGroupBox(Dialog)
        self.appBox.setGeometry(QtCore.QRect(0, 0, 391, 241))
        self.appBox.setObjectName("appBox")
        self.app_label = QtWidgets.QLabel(self.appBox)
        self.app_label.setGeometry(QtCore.QRect(150, 20, 200, 27))
        self.app_label.setObjectName("app_label")

        self.name_label = QtWidgets.QLabel(self.appBox)
        self.name_label.setGeometry(QtCore.QRect(30, 70, 61, 21))
        self.name_label.setObjectName("name_label")

        self.name = QtWidgets.QLineEdit(self.appBox)
        self.name.setGeometry(QtCore.QRect(100, 70, 200, 27))
        self.name.setObjectName("name")


        self.email_label = QtWidgets.QLabel(self.appBox)
        self.email_label.setGeometry(QtCore.QRect(30, 120, 61, 21))
        self.email_label.setObjectName("email_label")

        self.email = QtWidgets.QLineEdit(self.appBox)
        self.email.setGeometry(QtCore.QRect(100, 120, 200, 27))
        self.email.setObjectName("email")


        self.pwd_label = QtWidgets.QLabel(self.appBox)
        self.pwd_label.setGeometry(QtCore.QRect(30, 170, 61, 21))
        self.pwd_label.setObjectName("pwd_label")

        self.pwd = QtWidgets.QLineEdit(self.appBox)
        self.pwd.setGeometry(QtCore.QRect(100, 170, 200, 27))
        self.pwd.setObjectName("pwd")


        self.loginBtn = QtWidgets.QPushButton(Dialog)
        self.loginBtn.setGeometry(QtCore.QRect(40, 250, 75, 23))
        self.loginBtn.setMaximumSize(QtCore.QSize(75, 16777215))
        self.loginBtn.setObjectName("loginBtn")

        self.clearBtn = QtWidgets.QPushButton(Dialog)
        self.clearBtn.setGeometry(QtCore.QRect(250, 250, 75, 23))
        self.clearBtn.setMaximumSize(QtCore.QSize(75, 16777215))
        self.clearBtn.setObjectName("clearBtn")
        
        self.retranslateUi(Dialog)
        self.loginBtn.clicked.connect(Dialog.start)
        self.clearBtn.clicked.connect(Dialog.clear)
        QtCore.QMetaObject.connectSlotsByName(Dialog)
    
    def retranslateUi(self, Dialog):
        _translate = QtCore.QCoreApplication.translate
        Dialog.setWindowTitle(_translate("Dialog", "Shadow"))
        # self.appBox.setTitle(_translate("Dialog", "Shadow 登陆器"))
        self.app_label.setText(_translate("Dialog", "Shadow 登陆器"))

        self.name_label.setText(_translate("Dialog", "昵称"))
        self.email_label.setText(_translate("Dialog", "邮箱"))
        self.pwd_label.setText(_translate("Dialog", "设置密码"))

        self.pwd.setEchoMode(QtWidgets.QLineEdit.Password)
        conf = read_conf()
        self.name.setText(conf[0])
        self.email.setText(conf[1])
        self.pwd.setText(conf[2])


        self.loginBtn.setText(_translate("Dialog", "启动"))
        self.clearBtn.setText(_translate("Dialog", "清空"))

