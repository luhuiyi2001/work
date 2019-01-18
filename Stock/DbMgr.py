#!/usr/bin/python
# coding:utf-8

import os
import Config
import ConfigUtil
import Constants
import DB
import pymysql
import Common


class DbMgr:
    @staticmethod
    def connect():
        db = pymysql.connect(host=Config.DB_URL,
                             user=Config.DB_USER,
                             passwd=Config.DB_PWD,
                             db=Config.DB_NAME,
                             charset=Config.CHARSET_TYPE)
        return db

    @staticmethod
    def get_col_info(col_name):
        cur_index = DbMgr.allColName.index(col_name)
        return DbMgr.allColDetail[cur_index]

    @staticmethod
    def read_col_info():
        DbMgr.allColName = ConfigUtil.read_sections(Config.PATH_COL_INFO)
        col_num = len(DbMgr.allColName)
        DbMgr.allColDetail = []
        for i in range(col_num):
            cur_section = DbMgr.allColName[i]
            cur_options = ConfigUtil.read_options(Config.PATH_COL_INFO, cur_section)
            DbMgr.allColDetail.append(cur_options)
        # print(DbMgr.allColDetail)
        # for i in range(col_num):
        #     print(DbMgr.allColName[i], DbMgr.allColDetail[i])

    @staticmethod
    def create_table_to_db():
        # 建立数据库连接,剔除已入库的部分
        db = DbMgr.connect()
        cursor = db.cursor()
        c_len = len(DB.TABLE_ALL)
        for i in range(c_len):
            try:
                cur_table = DB.TABLE_ALL[i]
                # drop table
                sql_drop_table = DB.SQL_DROP_TABLE_FORMAT % cur_table
                # print(sql_drop_table)
                cursor.execute(sql_drop_table)
                db.commit()
                # create table
                cur_all_col_with_table = ConfigUtil.read_sections(os.path.join(Config.PATH_DB, cur_table))
                cur_sql_create_table = DbMgr.create_table_sql(cur_table, cur_all_col_with_table)
                # print(cur_sql_create_table)
                cursor.execute(cur_sql_create_table)
                db.commit()
            except Exception as err:
                print(err)
                continue
        cursor.close()
        db.close()

    @staticmethod
    def create_table_sql(table, col_arrays):
        col_num = len(col_arrays)
        sql_col_arrays = []
        for i in range(col_num):
            cur_col_name = col_arrays[i]
            cur_dict = DbMgr.get_col_info(cur_col_name)
            cur_type = cur_dict.get(Constants.COL_TYPE_TYPE)
            cur_max_num = cur_dict.get(Constants.COL_TYPE_MAX_NUM)
            cur_comment = cur_dict.get(Constants.COL_TYPE_COMMENT)
            cur_sql_col = DB.SQL_CREATE_COLUMN_FORMAT % (cur_col_name, cur_type, cur_max_num, cur_comment)
            sql_col_arrays.append(cur_sql_col)
        sql_all_col = ''.join(sql_col_arrays)
        sql_create_table = DB.SQL_CREATE_TABLE_FORMAT % (table, sql_all_col)
        # print(sql_create_table)
        return sql_create_table

    @staticmethod
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
            if value_type[i] == DB.DATA_TYPE_STR:
                value_format.append('"%s"' % str(cur_value).replace('"', '\\"'))
            elif value_type[i] == DB.DATA_TYPE_INT:
                value_format.append("%i" % float(cur_value))
            elif value_type[i] == DB.DATA_TYPE_FLOAT:
                value_format.append("%.2f" % float(cur_value))
        values_str = DB.SQL_COL_SEPARTOR.join(value_format)
        sql_insert = DB.SQL_INSERT_INTO % (table, all_column, values_str)
        return sql_insert

    @staticmethod
    def insert_to_db(data, table, cols, value_type):
        c_len = data.shape[0]
        # 建立数据库连接,剔除已入库的部分
        db = DbMgr.connect()
        cursor = db.cursor()
        for i in range(c_len):
            try:
                currow = list(data.iloc[i])
                # print(currow)
                sql_insert = DbMgr.get_sql_insert(table, cols, value_type, currow)
                print(sql_insert)
                cursor.execute(sql_insert)
                db.commit()
            except Exception as err:
                print(err)
                continue
        cursor.close()
        db.close()

    @staticmethod
    def import_stock_basic():
        data = Common.query_pro(DbMgr.pro, Constants.QUERY_INDEX_STOCK_BASIC)
        # 建立数据库连接,剔除已入库的部分
        DbMgr.insert_to_db(data, DB.TABLE_STOCK_BASIC, DB.STOCK_BASIC_COL_ALL, DB.STOCK_BASIC_COL_TYPE)
        print('Import Stock Basic Finished!')

    @staticmethod
    def import_trade_cal():
        data = Common.query_pro(DbMgr.pro, Constants.QUERY_INDEX_TRADE_CAL)
        # print(data)
        # 建立数据库连接,剔除已入库的部分
        DbMgr.insert_to_db(data, DB.TABLE_TRADE_CAL, DB.TRADE_CAL_COL_ALL, DB.TRADE_CAL_COL_TYPE)
        print('Import Trade Cal Finished!')

    @staticmethod
    def import_stock_company():
        data = Common.query_pro(DbMgr.pro, Constants.QUERY_INDEX_STOCK_COMPANY)
        # print(data)
        # 建立数据库连接,剔除已入库的部分
        DbMgr.insert_to_db(data, DB.TABLE_STOCK_COMPANY, DB.ALL_COL_STOCK_COMPANY, DB.ALL_TYPE_STOCK_COMPANY)
        print('Import Stock Company Finished!')

    @staticmethod
    def import_name_change():
        data = Common.query_pro(DbMgr.pro, Constants.QUERY_INDEX_NAMECHANGE)
        # print(data)
        # 建立数据库连接,剔除已入库的部分
        DbMgr.insert_to_db(data, DB.TABLE_NAMECHANGE, DB.ALL_COL_NAMECHANGE, DB.ALL_TYPE_NAMECHANGE)
        print('Import Name Change Finished!')


if __name__ == "__main__":
    DbMgr.read_col_info()
    DbMgr.create_table_to_db()
    DbMgr.pro = Common.pro_api()


