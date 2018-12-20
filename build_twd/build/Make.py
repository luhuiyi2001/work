#!/usr/bin/python
#coding:utf-8

from Constants import *
from ConfigUtil import *
from Model import *

import os
import subprocess

class Make():

    @staticmethod
    def doMakeCmd():
        os.chdir(Model.srcPath)
        if cmp(Model.mkType, HY_MK_TYPE_IMAGE) == 0:
            if cmp(Model.platform, HY_PLATFORM_MT6572) == 0 or cmp(Model.platform, HY_PLATFORM_MT6582) == 0:
                res = subprocess.call("./mkt_user.sh c && ./mkt_user.sh r lk k && ./mkt_user.sh bootimage && ./mkt_user.sh recoveryimage && ./mkt_user.sh r dr libcam.paramsmgr", shell = True)
            elif cmp(Model.platform, HY_PLATFORM_MT6582_92) == 0 or cmp(Model.platform, HY_PLATFORM_MT6592_92) == 0:
                res = subprocess.call("./tyMk.py user c && ./tyMk.py user r lk k && ./tyMk.py user bootimage && ./tyMk.py user recoveryimage && ./tyMk.py user r dr libcam.paramsmgr", shell = True)
            else:
                exit(1)
        elif cmp(Model.mkType, HY_MK_TYPE_LOGO) == 0:
            if cmp(Model.platform, HY_PLATFORM_MT6582) == 0:
                res = subprocess.call("./mkt_user.sh c && ./mkt_user.sh r lk k && ./mkt_user.sh bootimage", shell = True)
            elif cmp(Model.platform, HY_PLATFORM_MT6582_92) == 0:
                res = subprocess.call("./tyMk.py user c && ./tyMk.py user r lk k && ./tyMk.py user bootimage", shell = True)
            elif cmp(Model.platform, HY_PLATFORM_MT6572) == 0:
                res = subprocess.call("./mkt_user.sh c && ./mkt_user.sh r lk k && ./mkt_user.sh bootimage", shell = True)
            else:
                exit(1)
        elif cmp(Model.mkType, HY_MK_TYPE_PTGEN) == 0:
            if cmp(Model.platform, HY_PLATFORM_MT6572) == 0 or cmp(Model.platform, HY_PLATFORM_MT6582) == 0:
                res = subprocess.call("./mkt_user.sh ptgen", shell = True)
            elif cmp(Model.platform, HY_PLATFORM_MT6582_92) == 0:
                res = subprocess.call("./tyMk.py user ptgen ", shell = True)
            else:
                exit(1)
        elif cmp(Model.mkType, HY_MK_TYPE_ALL) == 0:
            if cmp(Model.platform, HY_PLATFORM_MT6572) == 0 or cmp(Model.platform, HY_PLATFORM_MT6582) == 0:
                res = subprocess.call("./mkt_user.sh n ", shell = True)
            elif cmp(Model.platform, HY_PLATFORM_MT6577) == 0 or cmp(Model.platform, HY_PLATFORM_MT6582_92) == 0 or cmp(Model.platform, HY_PLATFORM_MT6592_92) == 0:
                res = subprocess.call("./tyMk.py user n ", shell = True)
            elif cmp(Model.platform, HY_PLATFORM_MT6580) == 0 or cmp(Model.platform, HY_PLATFORM_MT6582_L0) == 0:
                hyMkFile = os.path.join(Model.srcPath, 'hyMkAll.sh')
                tmplHyMkFile = os.path.join(BUILD_PATH, Model.makeLmnoPath)
                subprocess.call('cp -vf ' + tmplHyMkFile + ' ' + hyMkFile, shell = True)
                res = subprocess.call("./hyMkAll.sh " + Model.project, shell = True)
            else:
                exit(1)
        #print res
        os.chdir(BUILD_PATH)
        #print os.getcwd()
        if res != 0:
            print 'Make Failed!'
            exit(1)

