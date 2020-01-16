import tushare as ts
import Constants
import DB
import Config


def pro_api():
    # 设置tushare pro的token并获取连接
    ts.set_token(Config.API_TOKEN)
    return ts.pro_api()


def query(pro, table, col_arrays):
    try:
        all_column = DB.SQL_COL_SEPARTOR.join(col_arrays)
        # print(table, all_column)
        if table == DB.TABLE_STOCK_BASIC:
            return pro.query(table, exchange='', list_status='L', fields=all_column)
        elif table == DB.TABLE_TRADE_CAL:
            return pro.query(table, start_date='20190101', end_date='20191231', fields=all_column)
        elif table == DB.TABLE_STOCK_COMPANY:
            return pro.stock_company(exchange='SZSE', fields=all_column)
        elif table == DB.TABLE_NAMECHANGE:
            return pro.namechange(ts_code='600848.SH', fields=all_column)
        elif table == DB.TABLE_HS_CONST:
            return pro.hs_const(hs_type='SZ', fields=all_column)
        elif table == DB.TABLE_NEW_SHARE:
            return pro.new_share(start_date='20180901', end_date='20181018', fields=all_column)
        elif table == DB.TABLE_DAILY:
            # return pro.query(table, ts_code='000001.SZ', start_date='20180701', end_date='20180718', fields=all_column)
            return pro.daily(trade_date='20180810', fields=all_column)
        elif table == DB.TABLE_WEEKLY:
            return pro.weekly(ts_code='000001.SZ', start_date='20181001', end_date='20181101', fields=all_column)
            # return pro.weekly(trade_date='20181123', fields=all_column)
        elif table == DB.TABLE_MONTHLY:
            return pro.monthly(ts_code='000001.SZ', start_date='20181001', end_date='20181101', fields=all_column)
            # return pro.monthly(trade_date='20181031', fields=all_column)
        elif table == DB.TABLE_PRO_BAR:
            # 取000001的前复权行情
            return ts.pro_bar(pro_api=pro, ts_code='000001.SZ', adj='qfq', start_date='20180101', end_date='20181011')
            # 取000001的后复权行情
            # return ts.pro_bar(pro_api=pro, ts_code='000001.SZ', adj='hfq', start_date='20180101', end_date='20181011')
            # 取000001的周线前复权行情
            # return ts.pro_bar(pro_api=pro, ts_code='000001.SZ', freq='W', adj='qfq', start_date='20180101', end_date='20181011')
            # 取000001的周线后复权行情
            # return ts.pro_bar(pro_api=pro, ts_code='000001.SZ', freq='W', adj='hfq', start_date='20180101', end_date='20181011')
            # 取000001的月线前复权行情
            # return ts.pro_bar(pro_api=pro, ts_code='000001.SZ', freq='M', adj='qfq', start_date='20180101', end_date='20181011')
            # 取000001的月线后复权行情
            # return ts.pro_bar(pro_api=pro, ts_code='000001.SZ', freq='M', adj='hfq', start_date='20180101', end_date='20181011')
        elif table == DB.TABLE_ADJ_FACTOR:
            # 提取000001全部复权因子
            return pro.adj_factor(ts_code='000001.SZ', trade_date='', fields=all_column)
            # 提取2018年7月18日复权因子
            # return pro.adj_factor(ts_code='', trade_date='20180818')
            # return pro.query(table, trade_date='20180818', fields=all_column)
        elif table == DB.TABLE_DAILY_BASIC:
            return pro.daily_basic(ts_code='', trade_date='20191130', fields=all_column)
            # return pro.query(table, ts_code='', trade_date='20180721', fields=all_column)
        elif table == DB.TABLE_SUSPEND:
            return pro.suspend(ts_code='600848.SH', suspend_date='', resume_date='', fields=all_column)
        elif table == DB.TABLE_HK_HOLD:
            # return pro.hk_hold(trade_date='20190930', exchange='SZ')
            return pro.hk_hold(trade_date='20190830', exchange='SZ')
            # exchange='SZ'
            # return pro.query(table, ts_code='', suspend_date='20180720', resume_date='', fields=all_column)
        #elif table == DB.TABLE_INCOME:

    except Exception as err:
        print(err)
        print('No DATA Code of ' + table)
        exit(1)
