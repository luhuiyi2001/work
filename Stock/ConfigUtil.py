#!/usr/bin/python
# coding:utf-8

import configparser
import Config


def read_sections(path):
    cf = configparser.ConfigParser()
    cf.read(path)
    return cf.sections()


def read_options(path, section):
    tmp_dict = {}
    cf = configparser.ConfigParser()
    cf.read(path)
    if not cf.has_section(section):
        return None
    opts = cf.options(section)
    for option in opts:
        tmp_dict[option] = cf.get(section, option)
    return tmp_dict


def read_config_info(path):
    # print(path)
    cf = configparser.ConfigParser()
    cf.read(path)
    # print(cf.sections())
    return cf


def read_col_info():
    cf = configparser.ConfigParser()
    cf.read(Config.CONFIG_PATH_COL_INFO)
    print(cf.sections())
    return cf


if __name__ == "__main__":
    read_col_info()
