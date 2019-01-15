import datetime
import tushare as ts
import pymysql
import Constants
import Config


def pro_api():
    # 设置tushare pro的token并获取连接
    ts.set_token(Constants.API_TOKEN)
    return ts.pro_api()


def connect():
    db = pymysql.connect(host=Config.DB_URL, user=Config.DB_USER, passwd=Config.DB_PWD, db=Config.DB_NAME, charset=Config.CHARSET_TYPE)
    return db


def import_stock_basic():
    pro = pro_api()
    try:
        data = pro.query('stock_basic', exchange='', list_status='L', fields='ts_code,symbol,name,area,industry,fullname,enname,market,exchange,curr_type,list_status,list_date,delist_date,is_hs')
        c_len = data.shape[0]
        # print(data.shape)
    except Exception as aa:
        print(aa)
        print('No DATA Code of Stock Basic')
    for i in range(c_len):
        cur_row_info = list(data.ix[i])
        print(cur_row_info)
    # 建立数据库连接,剔除已入库的部分
    # db = connect()
    # cursor = db.cursor()
