#!/usr/bin/python
# coding:utf-8

SQL_COL_SEPARTOR = ','

DATA_TYPE_STR = 'VARCHAR'
DATA_TYPE_INT = 'INT'
DATA_TYPE_FLOAT = 'DECIMAL'

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


TABLE_ALL = [TABLE_STOCK_BASIC, TABLE_TRADE_CAL, TABLE_STOCK_COMPANY, TABLE_NAMECHANGE]\
# TABLE_ALL = [TABLE_TRADE_CAL]

SQL_INSERT_INTO = 'INSERT INTO %s(%s) VALUES (%s)'
SQL_CREATE_COLUMN_FORMAT = """`%s` %s(%s) COMMENT '%s',"""
SQL_DROP_TABLE_FORMAT = """DROP TABLE IF EXISTS `%s`"""
SQL_CREATE_TABLE_FORMAT = """
CREATE TABLE `%s` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
%s
PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
"""
