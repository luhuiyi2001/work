import datetime
import tushare as ts
import pymysql
import Constants
import DB
import Config


def pro_api():
    # 设置tushare pro的token并获取连接
    ts.set_token(Config.API_TOKEN)
    return ts.pro_api()


def connect():
    db = pymysql.connect(host=Config.DB_URL, user=Config.DB_USER, passwd=Config.DB_PWD, db=Config.DB_NAME, charset=Config.CHARSET_TYPE)
    return db


def get_sql_insert(table, cols, value_type, values):
    all_column = DB.SQL_COL_SEPARTOR.join(cols)
    value_format = []
    type_len = len(value_type)
    # print(values)
    # print(value_type)
    for i in range(type_len):
        cur_value = values[i]
        if str(cur_value) == 'nan':
            cur_value = -1
        if value_type[i] == DB.COL_TYPE_STR:
            value_format.append('"%s"' % str(cur_value).replace('"', '\\"'))
        elif value_type[i] == DB.COL_TYPE_INT:
            value_format.append("%i" % float(cur_value))
        elif value_type[i] == DB.COL_TYPE_FLOAT:
            value_format.append("%.2f" % float(cur_value))
    values_str = DB.SQL_COL_SEPARTOR.join(value_format)
    sql_insert = DB.SQL_INSERT_INTO % (table, all_column, values_str)
    return sql_insert


def insert(data, table, cols, value_type):
    c_len = data.shape[0]
    # 建立数据库连接,剔除已入库的部分
    db = connect()
    cursor = db.cursor()
    for i in range(c_len):
        try:
            currow = list(data.iloc[i])
            # print(currow)
            sql_insert = get_sql_insert(table, cols, value_type, currow)
            print(sql_insert)
            cursor.execute(sql_insert)
            db.commit()
        except Exception as err:
            print(err)
            continue
    cursor.close()
    db.close()


def query_pro(pro, query_index):
    try:
        if query_index == Constants.QUERY_INDEX_STOCK_BASIC:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.STOCK_BASIC_COL_ALL)
            return pro.query(DB.TABLE_STOCK_BASIC, exchange='', list_status='L', fields=all_column)
        elif query_index == Constants.QUERY_INDEX_TRADE_CAL:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.TRADE_CAL_COL_ALL)
            return pro.query(DB.TABLE_TRADE_CAL, start_date='20190101', end_date='20191231', fields=all_column)
        elif query_index == Constants.QUERY_INDEX_STOCK_COMPANY:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.ALL_COL_STOCK_COMPANY)
            return pro.stock_company(exchange='SZSE', fields=all_column)
        elif query_index == Constants.QUERY_INDEX_NAMECHANGE:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.ALL_COL_NAMECHANGE)
            return pro.namechange(ts_code='600848.SH', fields=all_column)
    except Exception as err:
        print(err)
        print('No DATA Code of Stock Basic')
        exit(1)


def import_stock_basic():
    pro = pro_api()
    data = query_pro(pro, Constants.QUERY_INDEX_STOCK_BASIC)
    # 建立数据库连接,剔除已入库的部分
    insert(data, DB.TABLE_STOCK_BASIC, DB.STOCK_BASIC_COL_ALL, DB.STOCK_BASIC_COL_TYPE)
    print('Import Stock Basic Finished!')


def import_trade_cal():
    pro = pro_api()
    data = query_pro(pro, Constants.QUERY_INDEX_TRADE_CAL)
    # print(data)
    # 建立数据库连接,剔除已入库的部分
    insert(data, DB.TABLE_TRADE_CAL, DB.TRADE_CAL_COL_ALL, DB.TRADE_CAL_COL_TYPE)
    print('Import Trade Cal Finished!')


def import_stock_company():
    pro = pro_api()
    data = query_pro(pro, Constants.QUERY_INDEX_STOCK_COMPANY)
    # print(data)
    # 建立数据库连接,剔除已入库的部分
    insert(data, DB.TABLE_STOCK_COMPANY, DB.ALL_COL_STOCK_COMPANY, DB.ALL_TYPE_STOCK_COMPANY)
    print('Import Stock Company Finished!')


def import_name_change():
    pro = pro_api()
    data = query_pro(pro, Constants.QUERY_INDEX_NAMECHANGE)
    # print(data)
    # 建立数据库连接,剔除已入库的部分
    insert(data, DB.TABLE_NAMECHANGE, DB.ALL_COL_NAMECHANGE, DB.ALL_TYPE_NAMECHANGE)
    print('Import Name Change Finished!')


if __name__ == "__main__":
    import_name_change()
