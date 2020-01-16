#!/usr/bin/env python
# coding:utf-8

import requests
from bs4 import BeautifulSoup
import os

# 服务器反爬虫机制会判断客户端请求头中的User-Agent是否来源于真实浏览器，所以，我们使用Requests经常会指定UA伪装成浏览器发起请求
headers = {'user-agent': 'Mozilla/5.0'}


# 写文件
def writedoc(ss, i, ii):
    # 打开文件
    # 编码为utf-8
    with open("E:\\Python爬取的文件\\问题\\第" + str(ii) + "页\\" + "问题" + str(i) + ".txt", 'w', encoding='utf-8') as f:
        # 写文件
        f.write(ss)
    print("问题" + str(i) + "文件写入完成" + "\n")


# 根据详细页面url获取目标字符串
def geturl(url):
    # 请求详细页面
    r = requests.get(url, headers=headers)
    # 改编码
    r.encoding = "GB2312"
    soup = BeautifulSoup(r.text, "html.parser")
    # 找出类名为 info-zi mb15 下的所有p标签
    ans = soup.find_all(["p", ".info-zi mb15"])
    # 用来储存最后需要写入文件的字符串
    mlist = ""
    for tag in ans:
        # 获取p标签下的string内容，并进行目标字符串拼接
        mlist = mlist + str(tag.string)
    # 返回目标字符串
    return mlist


# 获取目标网址第几页
def getalldoc(ii):
    # 字符串拼接成目标网址
    #testurl = "https://www.zhiliti.com.cn/html/luoji/list7_" + str(ii) + ".html"
    testurl = "https://www.baidu.com/"
    #testurl = "C:\\Users\\twd\\Desktop\\深圳市宝安区议案建议提案在线v4_0--承办单位--城管（执法）局.htm"
    # 使用request去get目标网址
    res = requests.get(testurl, headers=headers)
    # 更改网页编码--------不改会乱码
    res.encoding = "GB2312"
    # 创建一个BeautifulSoup对象
    soup = BeautifulSoup(res.text, "html.parser")
    print(soup)
    # 找出目标网址中所有的small标签
    # 函数返回的是一个list
    ans = soup.find_all("small")
    print(ans)
    # 用于标识问题
    #cnt = 1
    # 先创建目录
    #mkdir("E:\\Python爬取的文件\\问题\\第" + str(ii) + "页\\")
    #for tag in ans:
        # 获取a标签下的href网址
        #string_ans = str(tag.a.get("href"))
        # 请求详细页面
        # 返回我们需要的字符串数据
        #string_write = geturl(string_ans)
        # 写文件到磁盘
        #writedoc(string_write, cnt, ii)
        #cnt = cnt + 1
    #print("第", ii, "页写入完成")


def mkdir(path):
    # 去除首位空格
    path = path.strip()
    # 去除尾部 \ 符号
    path = path.rstrip("\\")
    # 判断路径是否存在
    # 存在     True
    # 不存在   False
    isExists = os.path.exists(path)
    # 判断结果
    if not isExists:
        # 如果不存在则创建目录
        # 创建目录操作函数
        os.makedirs(path)
        return True
    else:
        # 如果目录存在则不创建，并提示目录已存在
        return False


def getall():
    getalldoc(0)
    #for i in range(1, 31, 1):
    #    getalldoc(i)


if __name__ == "__main__":
    getall()
