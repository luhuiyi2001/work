#!/usr/bin/python
#coding:utf-8

from Constants import *
from Common import *
from Model import *

import os
import shutil


class SrcMgr():
        
    @staticmethod
    def hyCreateSrcConfig(tmplFile, targetFile):
        with open(tmplFile, "r") as srcFile, open(targetFile, "w") as destFile:
            for line in srcFile:
                if HY_REPL_BOARD in line:
                    if Model.customBoard:
                        line = line.replace(HY_REPL_BOARD, Model.customBoard)
                    else:
                        line = line.replace(HY_REPL_BOARD, '')
                    
                if HY_REPL_CUST_NAME in line:
                    if Model.custName:
                        line = line.replace(HY_REPL_CUST_NAME, Model.custName)
                    else:
                        line = line.replace(HY_REPL_CUST_NAME, '')
                    
                if HY_REPL_NG in line:
                    if Model.ng:
                        line = line.replace(HY_REPL_NG, Model.ng)
                    else:
                        line = line.replace(HY_REPL_NG, '')
                        
                if HY_REPL_LCM in line:
                    if Model.lcm:
                        line = line.replace(HY_REPL_LCM, Model.lcm)
                    else:
                        line = line.replace(HY_REPL_LCM, '')
                
                if HY_REPL_CUST_COMMON in line:
                    if Model.custCommon:
                        line = line.replace(HY_REPL_CUST_COMMON, Model.custCommon)
                    else:
                        line = line.replace(HY_REPL_CUST_COMMON, '')
                
                
                if HY_REPL_CUST_CUSTOM in line:
                    if Model.custCustom:
                        line = line.replace(HY_REPL_CUST_CUSTOM, Model.custCustom)
                    else:
                        line = line.replace(HY_REPL_CUST_CUSTOM, '')
                    
                destFile.write(line)
        srcFile.close()
        destFile.close();
        print tmplFile, targetFile

    @staticmethod
    def hyCreateProjectConfig():
        print '-------- hyCreateProjectConfig ----'
        mSrcTyProjectConfig = os.path.join(Model.dirTmplProjConf, Model.board + '.mk')
        if not (os.path.exists(mSrcTyProjectConfig)):
            print 'TyProjectConfig Template Path isn\'t exist!'
            exit(1)

        if not (os.path.exists(Model.srcPath)):
            print 'Source Path isn\'t exist!'
            exit(1)

        SrcMgr.hyCreateSrcConfig(mSrcTyProjectConfig, Model.tyProjConfPath)

    @staticmethod
    def hyCheckProjectConfig():
        print '-------- hyCheckProjectConfig ----'
        Model.hyReadTyProjectConfig(Model.tyProjConfPath)
        
        if not Model.limitNg == None:
            limitNgArray = Model.limitNg.split(',')
            if not Model.ng in limitNgArray:
                print "NG is error: " + str(Model.ng)
                exit(1)
                
        if not Model.limitLcm == None:
            limitLcmArray = Model.limitLcm.split(',')
            if not Model.lcm in limitLcmArray:
                print "LCM is error: " + str(Model.lcm)
                exit(1)
                
        if isEmpty(Model.custName):
            print 'custName is empty: ' + str(Model.custName)
            exit(1)
            
        if not Model.custName in HY_CUST_NAME_NEUTRAL and cmp(Model.custName, HY_ZZTEMP) != 0:
            custCommonPath = os.path.join(Model.srcPath, Model.custCommonDir, Model.custName)
            custCustomPath = os.path.join(Model.srcPath, Model.custCustomDir, Model.custName)
            if not os.path.exists(custCommonPath) and not os.path.exists(custCustomPath):
                print 'custName is error: ' + str(Model.custName)
                exit(1)
    
    @staticmethod
    def hyCreateZztempConfig():
        print '-------- hyCreateZztempConfig ----'
        if not cmp(Model.custName, HY_ZZTEMP) == 0:
            return
        mSrcZZProjectConfig = os.path.join(Model.dirTmplZztemp, Model.zzProjConf)
        #print mSrcZZProjectConfig
        
        mDestZZDir = os.path.join(Model.srcPath, Model.custCustomDir, HY_ZZTEMP)
        if (os.path.exists(mDestZZDir)):
            shutil.rmtree(mDestZZDir)
        os.makedirs(mDestZZDir)
        
        mDestZZProjectConfig = os.path.join(mDestZZDir, Model.zzProjConf)
        #print mDestZZProjectConfig
        
        SrcMgr.hyCreateSrcConfig(mSrcZZProjectConfig, mDestZZProjectConfig)

    @staticmethod
    def hyCheckZztempConfig():
        print '-------- hyCheckZztempConfig ----'
        if not cmp(Model.custName, HY_ZZTEMP) == 0:
            return
        mDestZZDir = os.path.join(Model.srcPath, Model.custCustomDir, HY_ZZTEMP)
        mDestZZProjectConfig = os.path.join(mDestZZDir, Model.zzProjConf)
        Model.hyReadZZTempConfig(mDestZZProjectConfig)
        
        hyCheckOk = True
        if Model.custCommon:
            custCommonPath = os.path.join(Model.srcPath, Model.custCommonDir)
            for tmpCommon in Model.custCommon:
                tmpCustCommonPath = os.path.join(custCommonPath, tmpCommon)
                if not os.path.exists(tmpCustCommonPath):
                    print tmpCustCommonPath + ' isn\'t exists!'
                    hyCheckOk = False
                
        if Model.custCustom:
            custCustomPath = os.path.join(Model.srcPath, Model.custCustomDir)
            for tmpCustom in Model.custCustom:
                tmpCustCustomPath = os.path.join(custCustomPath, tmpCustom)
                if not os.path.exists(tmpCustCustomPath):
                    print tmpCustCustomPath + ' isn\'t exists!'
                    hyCheckOk = False
        if not hyCheckOk:
            exit(1)
    
