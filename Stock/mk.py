#!/usr/bin/env python
# coding:utf-8

import tushare as ts
import pandas as pd
import Common
import Constants

if __name__ == "__main__":
    # Common.import_stock_basic()

    all_col_nam = Constants.SQL_COL_SEPARTOR.join(Constants.STOCK_BASIC_COL_ALL)
    print(all_col_nam)
    values = ["601369.SH", "601369", "陕鼓动力", "陕西", "机械基件", "西安陕鼓动力股份有限公司", "Xi'An Shaangu Power Co., Ltd.", "主板", "SSE", "CNY", "L", "20100428", "123.1", "123.123"]
    value_format = []
    for i in range(len(Constants.STOCK_BASIC_COL_TYPE)):
        if Constants.STOCK_BASIC_COL_TYPE[i] == Constants.COL_TYPE_STR:
            value_format.append('"%s"' % str(values[i]))
        elif Constants.STOCK_BASIC_COL_TYPE[i] == Constants.COL_TYPE_INT:
            value_format.append("%i" % float(values[i]))
        elif Constants.STOCK_BASIC_COL_TYPE[i] == Constants.COL_TYPE_FLOAT:
            value_format.append("%.2f" % float(values[i]))

    values_str = Constants.SQL_COL_SEPARTOR.join(value_format)
    print(values_str)
    aa = Constants.SQL_INSERT_INTO % (Constants.TABLE_STOCK_BASIC, all_col_nam, values_str)
    print(aa)
    # data = [[1, 2, 3], [4, 5, 6]]
    # index = ['a', 'b']  # 行号
    # columns = ['c', 'd', 'e']  # 列号
    # df = pd.DataFrame(data, index=index, columns=columns)  # 生成一个数据框
    # print(df.loc['a'])
    # print(df.iloc[1])
    # print(df.loc[:, ['c']])
    # print(df.iloc[:, [0]])
    # print(df.ix[:, ['c']])
    # print(df.ix[:,[0]])
