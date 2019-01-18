#!/usr/bin/python
# coding:utf-8

TIME_FORMAT = '%Y%m%d%H%M'
DATE_FORMAT = '%Y%m%d'

SQL_COL_SEPARTOR = ','
SYMBOL_LF = '\n'
SQL_INSERT_INTO = 'INSERT INTO %s(%s) VALUES (%s)'

QUERY_INDEX_STOCK_BASIC = 1
QUERY_INDEX_TRADE_CAL = 2
QUERY_INDEX_STOCK_COMPANY = 3
QUERY_INDEX_NAMECHANGE = 4

DATA_TYPE_STR = 'VARCHAR'
DATA_TYPE_INT = 'INT'
DATA_TYPE_FLOAT = 'DECIMAL'

COL_INFO = 'col_info'
COL_TYPE_MAX_NUM = 'max_num'
COL_TYPE_TYPE = 'type'
COL_TYPE_COMMENT = 'comment'

# 股票列表
# 描述：获取基础信息数据，包括股票代码、名称、上市日期、退市日期等
TABLE_STOCK_BASIC = 'stock_basic'

# 交易日历
# 描述：获取各大交易所交易日历数据,默认提取的是上交所
TABLE_TRADE_CAL = 'trade_cal'

# 上市公司基本信息
# 描述：获取上市公司基础信息
TABLE_STOCK_COMPANY = 'stock_company'

# 股票曾用名
# 描述：历史名称变更记录
TABLE_NAMECHANGE = 'namechange'
