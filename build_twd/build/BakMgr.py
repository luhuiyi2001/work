#!/usr/bin/python
#coding:utf-8

from Constants import *
from Model import *

import os
import subprocess

class BakMgr():

    @staticmethod
    def hyBackupConfig():
        verPath = Model.verPath
        if not (os.path.exists(verPath)):
            os.makedirs(verPath)
        #zipPath = os.path.join(verPath, 'config.zip')
        configPath = os.path.join(verPath, 'config')
        if not (os.path.exists(configPath)):
            os.makedirs(configPath)
        custCommonPath = os.path.join(Model.srcPath, Model.custCommonDir)
        custCustomPath = os.path.join(Model.srcPath, Model.custCustomDir)
        #print custCustomPath
        #print Model.custCustom
        if Model.custCommon:
            for tmpItem in Model.custCommon:
                if not tmpItem:
                    continue
                tmpPath = os.path.join(custCommonPath, tmpItem)
                if (os.path.exists(tmpPath)):
                    #subprocess.call('zip -rj ' + zipPath + ' ' + tmpPath, shell = True)
                    subprocess.call('cp -rf ' + tmpPath + ' ' + configPath, shell = True)
        if Model.custCustom:
            for tmpItem in Model.custCustom:
                if not tmpItem:
                    continue
                tmpPath = os.path.join(custCustomPath, tmpItem)
                if (os.path.exists(tmpPath)):
                    print tmpPath, configPath
                    #subprocess.call('zip -rj ' + zipPath + ' ' + tmpPath, shell = True)
                    subprocess.call('cp -rf ' + tmpPath + ' ' + configPath, shell = True)
                
        tyProjectConfigPath = os.path.join(Model.srcPath, 'TyProjectConfig.mk')
        if (os.path.exists(tyProjectConfigPath)):
            subprocess.call('cp -rf ' + tyProjectConfigPath + ' ' + configPath, shell = True)
        
        configProjectConfigPath = os.path.join(Model.srcPath, 'twd', 'config', 'ProjectConfig.mk')
        if (os.path.exists(configProjectConfigPath)):
            tmpTwdConfigPath = os.path.join(configPath, 'twd', 'config')
            if not (os.path.exists(tmpTwdConfigPath)):
                os.makedirs(tmpTwdConfigPath)
            subprocess.call('cp -rf ' + configProjectConfigPath + ' ' + tmpTwdConfigPath, shell = True)
        
        #custName
        if not Model.custName in HY_CUST_NAME_NEUTRAL:
            custNamePath = os.path.join(custCommonPath, Model.custName)
            if not os.path.exists(custNamePath):
                custNamePath = os.path.join(custCustomPath, Model.custName)
            if os.path.exists(custNamePath):
                subprocess.call('cp -rf ' + custNamePath + ' ' + configPath, shell = True)
                
            
        curBuildPath = os.getcwd()
        os.chdir(configPath)
        subprocess.call('zip -r ../config.zip ./*', shell = True)
        subprocess.call('rm -rf ../config', shell = True)
        os.chdir(curBuildPath)
    