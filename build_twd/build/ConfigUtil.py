#!/usr/bin/python
#coding:utf-8

import ConfigParser

class ConfigUtil():
    @staticmethod
    def readConfigInfo(path, section):
        jDict = {}
        cf = ConfigParser.ConfigParser()
        cf.read(path)
        if not cf.has_section(section):
            return None
        opts = cf.options(section)
        for option in opts:
            jDict[option] = cf.get(section, option)
        return jDict

