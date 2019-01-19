import tushare as ts
import Constants
import DB
import Config


def pro_api():
    # 设置tushare pro的token并获取连接
    ts.set_token(Config.API_TOKEN)
    return ts.pro_api()


def query_pro(pro, table, col_arrays):
    try:
        all_column = DB.SQL_COL_SEPARTOR.join(col_arrays)
        if table == Constants.QUERY_INDEX_STOCK_BASIC:
            return pro.query(table, exchange='', list_status='L', fields=all_column)
        elif table == Constants.QUERY_INDEX_TRADE_CAL:
            return pro.query(table, start_date='20190101', end_date='20191231', fields=all_column)
        elif table == Constants.QUERY_INDEX_STOCK_COMPANY:
            return pro.stock_company(exchange='SZSE', fields=all_column)
        elif table == Constants.QUERY_INDEX_NAMECHANGE:
            return pro.namechange(ts_code='600848.SH', fields=all_column)
    except Exception as err:
        print(err)
        print('No DATA Code of Stock Basic')
        exit(1)
