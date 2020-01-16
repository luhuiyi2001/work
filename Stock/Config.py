#!/usr/bin/python
# coding:utf-8
import os
import Constants

BUILD_PATH = os.getcwd()
PATH_DB = os.path.join(BUILD_PATH, 'db')
PATH_COL_INFO = os.path.join(PATH_DB, Constants.COL_INFO)

API_TOKEN = '21f7872bf07e7a2ff06a31dc62e41ca3d8bcb73e5b205f1f282d94a1'
# DB INFO
# DB_URL = '127.0.0.1'
DB_URL = '114.55.67.172'
DB_USER = 'root'
DB_PWD = 'root'
DB_NAME = 'stock'
CHARSET_TYPE = 'utf8'
