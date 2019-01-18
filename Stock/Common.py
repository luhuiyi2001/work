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


def query_pro(pro, table, col_arrays):
    try:
        if table == Constants.QUERY_INDEX_STOCK_BASIC:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.STOCK_BASIC_COL_ALL)
            return pro.query(table, exchange='', list_status='L', fields=all_column)
        elif table == Constants.QUERY_INDEX_TRADE_CAL:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.TRADE_CAL_COL_ALL)
            return pro.query(table, start_date='20190101', end_date='20191231', fields=all_column)
        elif table == Constants.QUERY_INDEX_STOCK_COMPANY:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.ALL_COL_STOCK_COMPANY)
            return pro.stock_company(exchange='SZSE', fields=all_column)
        elif table == Constants.QUERY_INDEX_NAMECHANGE:
            all_column = DB.SQL_COL_SEPARTOR.join(DB.ALL_COL_NAMECHANGE)
            return pro.namechange(ts_code='600848.SH', fields=all_column)
    except Exception as err:
        print(err)
        print('No DATA Code of Stock Basic')
        exit(1)





if __name__ == "__main__":
    import_name_change()
