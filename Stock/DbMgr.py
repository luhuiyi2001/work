#!/usr/bin/python
# coding:utf-8

import os
import Config
import ConfigUtil
import Constants
import DB
import pymysql
import TuShare


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
        tmp_index = DbMgr.allColName.index(col_name)
        return DbMgr.allColDetail[tmp_index]

    @staticmethod
    def get_col_type(col_name):
        tmp_dict = DbMgr.get_col_info(col_name)
        return tmp_dict.get(DB.COL_TYPE_TYPE)

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
                print(sql_drop_table)
                cursor.execute(sql_drop_table)
                db.commit()
                # create table
                cur_all_col_with_table = ConfigUtil.read_sections(os.path.join(Config.PATH_DB, cur_table))
                print(cur_all_col_with_table)
                cur_sql_create_table = DbMgr._create_table_sql(cur_table, cur_all_col_with_table)
                print(cur_sql_create_table)
                cursor.execute(cur_sql_create_table)
                db.commit()
            except Exception as err:
                print(err)
                continue
        cursor.close()
        db.close()

    @staticmethod
    def _create_table_sql(table, col_arrays):
        col_num = len(col_arrays)
        sql_col_arrays = []
        for i in range(col_num):
            cur_col_name = col_arrays[i]
            cur_dict = DbMgr.get_col_info(cur_col_name)
            cur_type = cur_dict.get(DB.COL_TYPE_TYPE)
            cur_max_num = cur_dict.get(DB.COL_TYPE_MAX_NUM)
            cur_comment = cur_dict.get(DB.COL_TYPE_COMMENT)
            cur_sql_col = DB.SQL_CREATE_COLUMN_FORMAT % (cur_col_name, cur_type, cur_max_num, cur_comment)
            sql_col_arrays.append(cur_sql_col)
        sql_all_col = Constants.SYMBOL_LF.join(sql_col_arrays)
        sql_create_table = DB.SQL_CREATE_TABLE_FORMAT % (table, sql_all_col)
        # print(sql_create_table)
        return sql_create_table

    @staticmethod
    def _create_insert_sql(table, cols, value_type, values):
        all_column = DB.SQL_INSERT_COL_SEPARTOR.join(cols)
        value_format = []
        type_len = len(value_type)
        # print(values)
        # print(value_type)
        for i in range(type_len):
            cur_value = values[i]
            if str(cur_value) == 'nan' or str(cur_value) == 'None':
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
    def _insert_to_db(data, table, cols, value_type):
        print(data.columns.values)
        c_len = data.shape[0]
        # 建立数据库连接,剔除已入库的部分
        db = DbMgr.connect()
        cursor = db.cursor()
        for i in range(c_len):
            try:
                currow = list(data.iloc[i])
                # print(currow)
                sql_insert = DbMgr._create_insert_sql(table, cols, value_type, currow)
                # print(sql_insert)
                cursor.execute(sql_insert)
                db.commit()
            except Exception as err:
                print(err)
                continue
        cursor.close()
        db.close()

    @staticmethod
    def _query_from_db(sql_query):
        print(sql_query)
        # 建立数据库连接,剔除已入库的部分
        db = DbMgr.connect()
        cursor = db.cursor()
        try:
            # 执行SQL语句
            cursor.execute(sql_query)
            # 获取所有记录列表
            return cursor.fetchall()
        except Exception as err:
            print(err)
        # 关闭数据库连接
        db.close()

    @staticmethod
    def import_data_from_tu_share():
        pro = TuShare.pro_api()
        for cur_table in DB.TABLE_ALL:
            cur_all_col_with_table = ConfigUtil.read_sections(os.path.join(Config.PATH_DB, cur_table))
            data = TuShare.query(pro, cur_table, cur_all_col_with_table)
            print(data)
            cur_type_arrays = []
            for cur_col in cur_all_col_with_table:
                cur_type_arrays.append(DbMgr.get_col_type(cur_col))
            # 建立数据库连接,剔除已入库的部分
            DbMgr._insert_to_db(data, cur_table, cur_all_col_with_table, cur_type_arrays)
            print('Import ' + cur_table + ' Finished!')

    @staticmethod
    def import_hk_hold_data_from_tu_share(query_date):
        pro = TuShare.pro_api()
        cur_table = DB.TABLE_HK_HOLD
        cur_all_col_with_table = ConfigUtil.read_sections(os.path.join(Config.PATH_DB, cur_table))
        cur_type_arrays = []
        for cur_col in cur_all_col_with_table:
            cur_type_arrays.append(DbMgr.get_col_type(cur_col))
        data = pro.hk_hold(trade_date=query_date)
        print(data)
        DbMgr._insert_to_db(data, cur_table, cur_all_col_with_table, cur_type_arrays)
        print('Import ' + cur_table + '(' + query_date + ') :' + ' Finished!')

    @staticmethod
    def import_daily_basic_data_from_tu_share(query_date):
        pro = TuShare.pro_api()
        cur_table = DB.TABLE_DAILY_BASIC
        cur_all_col_with_table = ConfigUtil.read_sections(os.path.join(Config.PATH_DB, cur_table))
        cur_type_arrays = []
        for cur_col in cur_all_col_with_table:
            cur_type_arrays.append(DbMgr.get_col_type(cur_col))
        data = pro.daily_basic(trade_date=query_date)
        print(data)
        DbMgr._insert_to_db(data, cur_table, cur_all_col_with_table, cur_type_arrays)
        print('Import ' + cur_table + '(' + query_date + ') :' + ' Finished!')

    @staticmethod
    def import_hk_hold_data_from_tu_share_main():
        DbMgr.read_col_info()
        # DbMgr.create_table_to_db()
        # DbMgr.import_data_from_tu_share()
        DbMgr.import_hk_hold_data_from_tu_share('20200113')
        DbMgr.import_hk_hold_data_from_tu_share('20200114')

    @staticmethod
    def import_daily_basic_data_from_tu_share_main():
        DbMgr.read_col_info()
        # DbMgr.create_table_to_db()
        # DbMgr.import_data_from_tu_share()
        DbMgr.import_daily_basic_data_from_tu_share('20200109')
        DbMgr.import_daily_basic_data_from_tu_share('20200110')


if __name__ == "__main__":
    DbMgr.import_hk_hold_data_from_tu_share_main()
    # DbMgr.import_daily_basic_data_from_tu_share_main()


