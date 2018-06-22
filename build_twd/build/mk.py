#!/usr/bin/env python
#coding:utf-8

import sys
import os

from Constants import *
from Common import *
from Model import *
from SrcMgr import *
from VerMgr import *
from BakMgr import *
from Make import *

   
def hyReadInputInfo():
    print '---- hyReadInputInfo ----'
    Model.hyReadInputInfo()

def hyCreateBuildConfig():
    if not cmp(Model.isMake, HY_MAKE_TRUE) == 0:
        return
    print '---- hyCreateBuildConfig ----'
    SrcMgr.hyCreateProjectConfig()
    SrcMgr.hyCheckProjectConfig()
    SrcMgr.hyCreateZztempConfig()
    SrcMgr.hyCheckZztempConfig()

def hyMakeCmd():
    if not cmp(Model.isMake, HY_MAKE_TRUE) == 0:
        return
    print '---- hyMakeCmd ----'
    Make.doMakeCmd()
    
def hyReadBuildConfig():
    print '---- hyReadBuildConfig ----'
    Model.hyReadBuildConfig()
    
def hyBackupConfigData():
    print '---- hyBackupConfigData ----'
    BakMgr.hyBackupConfig()
    
def hyCreateVersion():
    print '---- hyCreateVersion ----'
    VerMgr.hyCreateVersion()
    
def hyCreateLog():
    print '---- hyCreateLog ----'

if __name__ == "__main__":
    print ROOT_PATH
    print BUILD_PATH
    #exit(1)
    hyReadInputInfo()
    hyCreateBuildConfig()
    hyMakeCmd()
    hyReadBuildConfig()
    hyCreateVersion()
    hyBackupConfigData()
    hyCreateLog()


