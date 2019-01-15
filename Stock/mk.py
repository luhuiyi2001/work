#!/usr/bin/env python
# coding:utf-8

import tushare as ts
import pandas as pd

if __name__ == "__main__":
    data = [[1, 2, 3], [4, 5, 6]]
    index = ['a', 'b']  # 行号
    columns = ['c', 'd', 'e']  # 列号
    df = pd.DataFrame(data, index=index, columns=columns)  # 生成一个数据框
    # print(df.loc['a'])
    # print(df.iloc[1])
    # print(df.loc[:, ['c']])
    # print(df.iloc[:, [0]])
    # print(df.ix[:, ['c']])
    # print(df.ix[:,[0]])
