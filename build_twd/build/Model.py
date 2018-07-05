#!/usr/bin/python
#coding:utf-8

from Constants import *
from ConfigUtil import *
from Common import *
import os
import re
import time

class Model():
    
    @staticmethod
    def hyReadInputInfo():
        print '-------- Start Read Input Info'
        Model.hyInitConfigInfo()
        
        #Version Info
        versionInfo = ConfigUtil.readConfigInfo(Model.verConfPath, HY_VERSION_INFO)
        Model.custom = hyReadValueWithExit(versionInfo, HY_OP_CUSTOM)
        Model.product = hyReadValueWithExit(versionInfo, HY_OP_PRODUCT)
        Model.mkType = hyReadValueWithExit(versionInfo, HY_OP_MK_TYPE)
        if not Model.mkType in HY_MK_TYPE_ARRAY:
            print Model.mkType + " isn\'t in " + str(HY_MK_TYPE_ARRAY)
        Model.isMake = hyReadValueWithExit(versionInfo, HY_OP_MAKE)
        print Model.isMake

        Model.hyInitCustomInfo()
        Model.hyInitProductInfo()
        Model.hyInitPlatformInfo()
        Model.hyInitBoardInfo()
        Model.hyInitConfKeyInfo()
        
        Model.ng = hyReadValueWithEmpty(versionInfo, HY_OP_NG)
        Model.lcm = hyReadValueWithEmpty(versionInfo, HY_OP_LCM)
        Model.customBoard = hyReadValueWithEmpty(versionInfo, HY_OP_BOARD)
        Model.custName = hyReadValueWithEmpty(versionInfo, HY_OP_CUST_NAME)
        Model.band = 'B125' #TY0706
        Model.custCommon = hyReadValueWithEmpty(versionInfo, HY_OP_CUST_COMMON)
        Model.custCustom = hyReadValueWithEmpty(versionInfo, HY_OP_CUST_CUSTOM)
        inputLog = []
        inputLog.append('Custom=' + str(Model.custom))
        inputLog.append('Product=' + str(Model.product))
        inputLog.append('MkType=' + str(Model.mkType))
        inputLog.append('NG=' + str(Model.ng))
        inputLog.append('lCM=' + str(Model.lcm))
        inputLog.append('CustomBoard=' + str(Model.customBoard))
        inputLog.append('CustName=' + str(Model.custName))
        inputLog.append('Band=' + str(Model.band))
        print ','.join(inputLog)
        print 'CustCommon=' + str(Model.custCommon)
        print 'CustCustom=' + str(Model.custCustom)
        
        Model.srcPath = os.path.join(ROOT_PATH, Model.src) #../K9601_6582
        Model.tyProjConfPath = os.path.join(Model.srcPath, HY_FILE_TY_PROJECT_CONFIG)
        Model.custCommonPath = os.path.join(Model.srcPath, Model.custCommonDir)
        Model.custCustomPath = os.path.join(Model.srcPath, Model.custCustomDir)
        
        if isEmpty(Model.lcm) and not isEmpty(Model.defaultLcm):
            Model.lcm = Model.defaultLcm
        
        #if not isEmpty(Model.includeCustCustom):
        #    if not cmp(Model.custName, Model.includeCustCustom) == 0:
        #        
            
        print '-------- End Read Input Info'

    @staticmethod
    def hyReadBuildConfig():
        print '-------- hyReadBuildConfig'
        Model.hyReadTyProjectConfig(Model.tyProjConfPath)
        
        if isEmpty(Model.custName):
            Model.custCommon = ''
            Model.custCustom = ''
        #    Model.custName = Model.neutralName
        custNameProjConfigPath = ''
        custCommonPath = os.path.join(Model.custCommonPath, Model.custName, Model.zzProjConf)
        if os.path.exists(custCommonPath):
            custNameProjConfigPath = custCommonPath
        custCustomPath = os.path.join(Model.custCustomPath, Model.custName, Model.zzProjConf)
        if os.path.exists(custCustomPath):
            custNameProjConfigPath = custCustomPath
        if not isEmpty(custNameProjConfigPath):
            Model.hyReadZZTempConfig(custNameProjConfigPath)
        
    @staticmethod
    def hyInitConfigInfo():
        #Config Info
        configInfo = ConfigUtil.readConfigInfo(HY_PATH_CONFIG_INI, HY_CONFIG)
        Model.verConfPath = configInfo[HY_OP_PATH_VERSION_CONF]
        Model.custConfPath = configInfo[HY_OP_PATH_CUSTOM_CONF]
        Model.productConfPath = configInfo[HY_OP_PATH_PRODUCT_CONF]
        Model.platConfPath = configInfo[HY_OP_PATH_PLATFORM_CONF]
        Model.boardPath = configInfo[HY_OP_PATH_BOARD_CONF]
        Model.confKeyPath = configInfo[HY_OP_PATH_CONF_KEY]
        Model.verRenamePath = configInfo[HY_OP_PATH_VER_RENAME_CONF]
        Model.verFileConfPath = configInfo[HY_OP_PATH_VER_FILE_CONF]
        Model.dirTmplProjConf = configInfo[HY_OP_DIR_TMPL_PROJ_CONF]
        Model.dirTmplZztemp = configInfo[HY_OP_DIR_TMPL_ZZTEMP]
        Model.makeLmnoPath = configInfo[HY_OP_PATH_MAKE_LMNO_SH]
        Model.dirVerRepl = os.path.join(BUILD_PATH, configInfo[HY_OP_DIR_VERSION_REPL])
        Model.verRootPath = os.path.join(ROOT_PATH, configInfo[HY_OP_DIR_VERSION]) #../versions
        #Model.buildVerDir = os.path.join(BUILD_PATH, Model.dirVerRepl) #data/version_repl
    
    @staticmethod
    def hyInitCustomInfo():
        #Custom Info
        customInfo = hyReadConfigDictWithExit(Model.custConfPath, Model.custom)
        Model.cnName = hyReadValueWithExit(customInfo, HY_OP_CN_NAME)
    
    @staticmethod
    def hyInitProductInfo():
        #Product Info
        productInfo = hyReadConfigDictWithExit(Model.productConfPath, Model.product)
        Model.platform = hyReadValueWithExit(productInfo, HY_OP_PLATFORM)
        Model.board = hyReadValueWithExit(productInfo, HY_OP_BOARD)
        Model.verForm = hyReadValueWithExit(productInfo, HY_OP_VER_FORM)
        
    @staticmethod
    def hyInitPlatformInfo():
        #Platform Info
        platformInfo = hyReadConfigDictWithExit(Model.platConfPath, Model.platform)
        Model.project = hyReadValueWithExit(platformInfo, HY_OP_PROJECT)
        Model.src = hyReadValueWithExit(platformInfo, HY_OP_SRC)
        Model.custDir = hyReadValueWithExit(platformInfo, HY_OP_CUST_DIR)
        Model.custBoardDir = hyReadValueWithExit(platformInfo, HY_OP_CUST_BOARD_DIR)
        Model.custCommonDir = hyReadValueWithExit(platformInfo, HY_OP_CUST_COMMON_DIR)
        Model.custCustomDir = hyReadValueWithExit(platformInfo, HY_OP_CUST_CUSTOM_DIR)
        Model.zzProjConf = hyReadValueWithExit(platformInfo, HY_OP_FILE_ZZ_PROJ_CONF)
        Model.neutralName = hyReadValueWithExit(platformInfo, HY_OP_NEUTRAL_NAME)

    @staticmethod
    def hyInitConfKeyInfo():
        #Conf Key Info
        confKeyInfo = hyReadConfigDictWithExit(Model.confKeyPath, Model.platform)
        Model.keyBoard = hyReadValue(confKeyInfo, HY_OP_BOARD)
        Model.keyCustName = hyReadValue(confKeyInfo, HY_OP_CUST_NAME)
        Model.keyNg = hyReadValue(confKeyInfo, HY_OP_NG)
        Model.keyLcm = hyReadValue(confKeyInfo, HY_OP_LCM)
        Model.keySim = hyReadValue(confKeyInfo, HY_OP_SIM)
        Model.keyBand = hyReadValue(confKeyInfo, HY_OP_BAND)
        Model.keyAmp = hyReadValue(confKeyInfo, HY_OP_AMP)
        Model.keyCustCommon = hyReadValue(confKeyInfo, HY_OP_CUST_COMMON)
        Model.keyCustCustom = hyReadValue(confKeyInfo, HY_OP_CUST_CUSTOM)
    
    @staticmethod
    def hyInitBoardInfo():
        #Board Info
        boardInfo = hyReadConfigDictWithExit(Model.boardPath, Model.board)
        Model.limitNg = hyReadValue(boardInfo, HY_OP_LIMIT_NG)
        Model.limitLcm = hyReadValue(boardInfo, HY_OP_LIMIT_LCM)
        Model.includeCustCustom = hyReadValue(boardInfo, HY_OP_INCLUDE_CUST_CUSTOM)
        Model.defaultLcm = hyReadValue(boardInfo, HY_OP_DEFAULT_LCM)
    
    @staticmethod
    def hyReadTyProjectConfig(projConfPath):
        tySetEnv(projConfPath)
        Model.customBoard = tyGetEnv(Model.keyBoard, '')
        Model.ng = tyGetEnv(Model.keyNg, '')
        Model.lcm = tyGetEnv(Model.keyLcm, '')
        Model.custName = tyGetEnv(Model.keyCustName, '')
        Model.band = 'B125' #TY0706
        print Model.customBoard,Model.ng,Model.lcm,Model.custName
        
        
    @staticmethod
    def hyReadZZTempConfig(zzConfPath):
        tySetEnv(zzConfPath)
        Model.custCommon = tyGetEnv(Model.keyCustCommon, '')
        Model.custCustom = tyGetEnv(Model.keyCustCustom, '')
        #print Model.custCommon,Model.custCustom
        if Model.custCommon:
            Model.custCommon = Model.custCommon.split(',')
        if Model.custCustom:
            Model.custCustom = Model.custCustom.split(',')
        print Model.custCommon,Model.custCustom