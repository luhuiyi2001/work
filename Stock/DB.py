#!/usr/bin/python
# coding:utf-8

SQL_COL_SEPARTOR = ','
SQL_INSERT_COL_SEPARTOR = '`,`'

DATA_TYPE_STR = 'VARCHAR'
DATA_TYPE_INT = 'INT'
DATA_TYPE_FLOAT = 'DECIMAL'

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

# 沪深股通成份股
# 描述：获取沪股通、深股通成分数据
TABLE_HS_CONST = 'hs_const'

# IPO新股列表
# 描述：获取新股上市列表数据
# 限量：单次最大2000条，总量不限制
TABLE_NEW_SHARE = 'new_share'

# 日线行情
# 更新时间：交易日每天15点～16点之间
# 调取说明：每分钟内最多调取200次，超过5000积分无限制
# 描述：获取股票行情数据，或通过通用行情接口获取数据，包含了前后复权数据．
TABLE_DAILY = 'daily'

# 周线行情
# 接口：weekly
# 描述：获取A股周线行情
# 限量：单次最大3700，总量不限制
TABLE_WEEKLY = 'weekly'

# 月线行情
# 接口：monthly
# 描述：获取A股月线数据
# 限量：单次最大3700，总量不限制
TABLE_MONTHLY = 'monthly'

# A股复权行情
# 接口名称 ：pro_bar
# 接口说明 ：复权行情通过通用行情接口实现，利用Tushare Pro提供的复权因子进行计算，目前暂时只在SDK中提供支持，http方式无法调取。
# Python SDK版本要求： >= 1.2.17
TABLE_PRO_BAR = 'pro_bar'

# 复权因子
# 接口：adj_factor
# 更新时间：早上9点30分
# 描述：获取股票复权因子，可提取单只股票全部历史复权因子，也可以提取单日全部股票的复权因子。
TABLE_ADJ_FACTOR = 'adj_factor'

# 每日指标
# 接口：daily_basic
# 更新时间：交易日每日15点～17点之间
# 描述：获取全部股票每日重要的基本面指标，可用于选股分析、报表展示等。
TABLE_DAILY_BASIC = 'daily_basic'

# 停复牌信息
# 接口：suspend
# 更新时间：不定期
# 描述：获取股票每日停复牌信息
TABLE_SUSPEND = 'suspend'

# 利润表
# 接口：income
# 描述：获取上市公司财务利润表数据
TABLE_INCOME = 'income'

# TABLE_ALL = [TABLE_STOCK_BASIC, TABLE_TRADE_CAL, TABLE_STOCK_COMPANY, TABLE_NAMECHANGE]
TABLE_ALL = [TABLE_INCOME]


SQL_INSERT_INTO = 'INSERT INTO `%s`(`%s`) VALUES (%s)'
SQL_CREATE_COLUMN_FORMAT = """`%s` %s(%s) COMMENT '%s',"""
SQL_DROP_TABLE_FORMAT = """DROP TABLE IF EXISTS `%s`"""
SQL_CREATE_TABLE_FORMAT = """
CREATE TABLE `%s` (
`id` INT(11) NOT NULL AUTO_INCREMENT,
%s
PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
"""
