import datetime
import tushare as ts
import pymysql
import Constants
import Config


def pro_api():
    # 设置tushare pro的token并获取连接
    ts.set_token(Config.API_TOKEN)
    return ts.pro_api()


def connect():
    db = pymysql.connect(host=Config.DB_URL, user=Config.DB_USER, passwd=Config.DB_PWD, db=Config.DB_NAME, charset=Config.CHARSET_TYPE)
    return db


def get_sql_insert(table, cols, value_type, values):
    all_column = Constants.SQL_COL_SEPARTOR.join(cols)
    value_format = []
    type_len = len(value_type)
    for i in range(type_len):
        if value_type[i] == Constants.COL_TYPE_STR:
            value_format.append('"%s"' % str(values[i]))
        elif value_type[i] == Constants.COL_TYPE_INT:
            value_format.append("%i" % float(values[i]))
        elif value_type[i] == Constants.COL_TYPE_FLOAT:
            value_format.append("%.2f" % float(values[i]))
    values_str = Constants.SQL_COL_SEPARTOR.join(value_format)
    sql_insert = Constants.SQL_INSERT_INTO % (table, all_column, values_str)
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
            # print(sql_insert)
            cursor.execute(sql_insert)
            db.commit()
        except Exception as err:
            print(err)
            continue
    cursor.close()
    db.close()


def query_pro(pro, query_index):
    try:
        if query_index == 1:
            all_column = Constants.SQL_COL_SEPARTOR.join(Constants.STOCK_BASIC_COL_ALL)
            return pro.query(Constants.TABLE_STOCK_BASIC, exchange='', list_status='L', fields=all_column)
    except Exception as err:
        print(err)
        print('No DATA Code of Stock Basic')
        exit(1)


def import_stock_basic():
    pro = pro_api()
    data = query_pro(pro, Constants.QUERY_INDEX_STOCK_BASIC)
    # 建立数据库连接,剔除已入库的部分
    insert(data, Constants.TABLE_STOCK_BASIC, Constants.STOCK_BASIC_COL_ALL, Constants.STOCK_BASIC_COL_TYPE)
    print('Import Stock Basic Finished!')


if __name__ == "__main__":
    import_stock_basic()
