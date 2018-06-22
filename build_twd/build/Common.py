#!/usr/bin/python

import os
import sys
import re
import commands

from Constants import *
from ConfigUtil import *

def getEnv(key):
    return os.getenv(key)

def setEnv(key,value):
    #print 'setEnv key='+key+',value='+value
    #os.putenv(key,value)
    os.environ[key] = value

def tyGetEnv(key,defValue):
    value = os.getenv(key)
    if value is None or len(value) == 0:
        value = defValue
    return value

def tySetEnv(prjConfPath):
    confFile = open(prjConfPath,'r')
    pattern = re.compile("^(\S+)\s*=\s*(\S+)")
    for line in confFile:
        m = pattern.match(line)
        if m:
            #print m.group()
            if m.lastindex != 2:
                continue
            setEnv(m.group(1),m.group(2))
            #print m.group(1),m.group(2)
    confFile.close()

def tyGetEnvBoolean(key):
    value = getEnv(key)
    if value is None or len(value) == 0:
        return False
    return cmp(value,'yes') == 0

def getConfigValue(confPath,confKey):
    confValue = None
    confFile = open(confPath,'r')
    pattern = re.compile("^(\S+)\s*:?=\s*(\S+)")
    for line in confFile:
        m = pattern.match(line)
        if m:
            #print m.group()
            if m.lastindex != 2:
                continue
            if cmp(confKey,m.group(1)) == 0:
                confValue = m.group(2)
                break;
    confFile.close()
    return confValue

def showMessage(tag,msg):
    print '['+tag+'] '+msg

def showError(tag,msg):
    showMessage(tag,msg)
    exit()

def hyReadConfigDictWithExit(path, section):
    hyDict = ConfigUtil.readConfigInfo(path, section)
    if hyDict == None:
        print "Section [" + section + "] isn\'t found!"
        exit(1)
    return hyDict
    
def hyReadValueWithExit(hyDict, hyKey):
    if not hyDict.has_key(hyKey):
        print "Option [" + hyKey + "] isn\'t found!"
        exit(1)
    return hyDict[hyKey]
    
def hyReadValue(hyDict, hyKey):
    if not hyDict.has_key(hyKey):
        return None
    return hyDict[hyKey]
    
def hyReadValueWithEmpty(hyDict, hyKey):
    if not hyDict.has_key(hyKey):
        return ''
    return hyDict[hyKey]
    
def findKeyValue(file, key):
    value = ""
    if not (os.path.exists(file)):
        return None
    with open(file, "r") as srcFile:
        for line in srcFile:
            #print '1: ',line
            #delete space
            line = line.replace(' ', '')
            line = line.replace('\t', '')
            line = line.strip('\n')
            #print '2: ',line
            if line.startswith('#'):
                continue
            #print '3: ',line
            keyValueArray = line.split('=')
            #print '4: ',keyValueArray
            if cmp(keyValueArray[0], key) == 0:
                value = keyValueArray[1]
                #print '5: ',value
                break
    srcFile.close()
    return value
    
def isEmpty(value):
    if value is None or len(value) < 1:
        return True
    return False