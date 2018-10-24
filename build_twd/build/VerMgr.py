#!/usr/bin/python
#coding:utf-8

from Constants import *
from Common import *
from Model import *

import os
import subprocess

class VerMgr():

    @staticmethod
    def hyInitVersionInfo():
        print '---- hyInitVersionInfo ----'
        Model.ptName = VerMgr.getPtName() #单分区/双分区/MTP
        Model.ngName = VerMgr.getNgName()
        Model.lcmName = VerMgr.getLcmName()
        Model.simName = VerMgr.getSimName()
        
        VerMgr.hyInitVersionPath()
        
        VerMgr.hyUpdateVersionName()
        
    @staticmethod
    def hyCreateVersion():
        print '---- hyCreateVersion ----'
        VerMgr.hyInitVersionInfo()
        #versions/YZ云志/201805311721_F0080_mt6580_3g_7_0_1g_user_180531_spi.bin
        if not (os.path.exists(Model.verPath)):
            os.makedirs(Model.verPath)
            subprocess.call('chmod -R 777 ' + Model.verPath, shell = True)
        zipPath = os.path.join(Model.verPath, Model.zipFileName + '.zip')
        for verFile in Model.verZipFiles:
            if not (os.path.exists(verFile)):
                print verFile + ' isn\'t exists!'
            else:
                subprocess.call('zip -rj ' + zipPath + ' ' + verFile, shell = True)

        for verFile in Model.verFiles:
            if not (os.path.exists(verFile)):
                print verFile + ' isn\'t exists!'
            else:
                subprocess.call('cp -rvf ' + verFile + ' ' + Model.verPath, shell = True)
        
    @staticmethod
    def getPtName():
        ptType = HY_PART_TYPE_SINGLE
        if Model.custCommon and 'PT_MERGE_AND_MTP' in Model.custCommon:
            ptMergeConfPath = os.path.join(Model.srcPath, Model.custCommonDir, 'PT_MERGE_AND_MTP', 'ProjectConfig.mk')
            
        elif Model.custCustom and 'PT_MERGE_AND_MTP' in Model.custCustom:
            ptMergeConfPath = os.path.join(Model.srcPath, Model.custCustomDir, 'PT_MERGE_AND_MTP', 'ProjectConfig.mk')
        else:
            ptMergeConfPath = ''
        print ptMergeConfPath
        if ptMergeConfPath:
            value = findKeyValue(ptMergeConfPath, 'MTK_SHARED_SDCARD')
            if cmp(value, 'yes') == 0:
                return HY_PART_TYPE_SINGLE
            elif cmp(value, 'no') == 0:
                return HY_PART_TYPE_DOUBLE
        
        if cmp(Model.platform, HY_PLATFORM_MT6582) == 0 or cmp(Model.platform, HY_PLATFORM_MT6582_92) == 0:
            ptType = HY_PART_TYPE_SINGLE
        else:
            ptType = HY_PART_TYPE_DOUBLE
        return ptType
        
    @staticmethod
    def getNgName():
        if cmp(Model.platform, HY_PLATFORM_MT6582_92) == 0:
            if cmp(Model.customBoard, 'K1001_800X1280_Support_3G') == 0 or cmp(Model.customBoard, 'K1001_800X1280_Support_PG_3G') == 0:
                ngName = HY_NG_TYPE_3G
            else:
                ngName = HY_NG_TYPE_2G
        else:
            ngName = HY_NG_TYPE_3G
            
        if cmp(Model.ng, HY_NG_TYPE_2G) == 0 or cmp(Model.ng, HY_K1001_COM_MODEM_2G) == 0:
            ngName = HY_NG_TYPE_2G
        elif cmp(Model.ng, HY_K1001_MODEM_3G) == 0:
            ngName = HY_NG_TYPE_3G
            
        return ngName
        
    @staticmethod
    def getLcmName():
        lcmName = Model.lcm
        if cmp(Model.lcm, '') == 0 or cmp(Model.lcm, 'HD_Q8') == 0:
            lcmName = 'HD'
        return lcmName
        
    @staticmethod
    def getSimName():
        return '2'
        
    @staticmethod
    def hyUpdateVersionName():
        Model.renameInfo = ConfigUtil.readConfigInfo(Model.verRenamePath, HY_VERSION_RENAME)
        mInfoList = []
        verName = Model.verForm
        verName = verName.replace(HY_VER_REPL_PRODUCT, Model.product)
        verName = verName.replace(HY_VER_REPL_NG, Model.ngName)
        verName = verName.replace(HY_VER_REPL_BAND, Model.band)
        verName = verName.replace(HY_VER_REPL_LCM, Model.lcmName)
        verName = verName.replace(HY_VER_REPL_SIM, Model.simName)
        verName = verName.replace(HY_VER_REPL_TIME, time.strftime(DATE_FORMAT))
        verName = verName.replace(HY_VER_REPL_SDCARD, Model.ptName)
        
        tmpBoardRenameValue = Model.renameInfo.get(Model.customBoard.lower())
        if tmpBoardRenameValue == None:
            verName = verName.replace(HY_VER_REPL_BOARD, Model.customBoard)
        elif tmpBoardRenameValue != '':
            verName = verName.replace(HY_VER_REPL_BOARD, tmpBoardRenameValue)
        
        mInfoList.append(verName)
        if not Model.custName in HY_CUST_NAME_NEUTRAL:
            if cmp(Model.custName, HY_ZZTEMP) == 0:
                if Model.custCommon:
                    for tmpItem in Model.custCommon:
                        tmpRenameValue = Model.renameInfo.get(tmpItem.lower())
                        if tmpRenameValue == None:
                            mInfoList.append(tmpItem)
                        elif tmpRenameValue != '':
                            mInfoList.append(tmpRenameValue)
                if Model.custCustom:
                    for tmpItem in Model.custCustom:
                        tmpRenameValue = Model.renameInfo.get(tmpItem.lower())
                        if tmpRenameValue == None:
                            mInfoList.append(tmpItem)
                        elif tmpRenameValue != '':
                            mInfoList.append(tmpRenameValue)
            else:
                if cmp(Model.platform, HY_PLATFORM_MT6580) != 0:
                    tmpRenameValue = Model.renameInfo.get(Model.custName.lower())
                    if tmpRenameValue == None:
                        mInfoList.append(Model.custName)
                    elif tmpRenameValue != '':
                        mInfoList.append(tmpRenameValue)
        verName = '_'.join(mInfoList)
        verName = verName.replace('CameraSupport_', '')
        verName = verName.replace('MODEM_2G_706', '')
        #verName = verName.replace('LCD_1024_600_K0708', 'K0708_1024x600')
        #verName = verName.replace('K1001-800X1280-Support_PG', 'K1001_800X1280_82苹果版')
        #verName = verName.replace('K1001-800X1280-Support', 'K1001_800X1280_82普通版')
        #verName = verName.replace('K1001_800X1280_Support_PG', 'K1001_800X1280_92苹果版')
        #verName = verName.replace('K1001_800X1280_Support', 'K1001_800X1280_92普通版')
        
        Model.zipFileName = verName
        Model.verPath = os.path.join(Model.verRootPath, Model.custom + Model.cnName, time.strftime(TIME_FORMAT) + '_' + verName)
        #return verName
        #mVerPath = os.path.join(Model.verRootPath, Model.custom + Model.cnName, mVerFileName)
    
    @staticmethod
    def hyInitVersionPath():
        if cmp(Model.mkType, HY_MK_TYPE_IMAGE) == 0:
            allVerFiles = ConfigUtil.readConfigInfo(Model.verFileConfPath, HY_IMG)
        elif cmp(Model.mkType, HY_MK_TYPE_PTGEN) == 0:
            allVerFiles = ConfigUtil.readConfigInfo(Model.verFileConfPath, HY_PTGEN)
        elif cmp(Model.mkType, HY_MK_TYPE_LOGO) == 0:
            allVerFiles = ConfigUtil.readConfigInfo(Model.verFileConfPath, HY_LOGO)
        else:
            allVerFiles = ConfigUtil.readConfigInfo(Model.verFileConfPath, HY_COMMON)
            verFilesWithPlatform = ConfigUtil.readConfigInfo(Model.verFileConfPath, Model.platform)
            allVerFiles.update(verFilesWithPlatform)
            verFilesWithProduct = ConfigUtil.readConfigInfo(Model.verFileConfPath, Model.product)
            if verFilesWithProduct:
                allVerFiles.update(verFilesWithProduct)
            verFilesWithBoard = ConfigUtil.readConfigInfo(Model.verFileConfPath, Model.customBoard)
            if verFilesWithBoard:
                allVerFiles.update(verFilesWithBoard)
        outPath = os.path.join(Model.srcPath, 'out/target/product', Model.project)
        
        noZipKeys = ''
        if HY_NO_ZIP_FILES in allVerFiles:
            noZipKeys = allVerFiles[HY_NO_ZIP_FILES]
            if not isEmpty(noZipKeys):
                noZipKeys = noZipKeys.split(',')
        
        Model.verFiles = []
        Model.verZipFiles = []
        for tmpFileKey in allVerFiles.keys():
            if cmp(tmpFileKey, HY_NO_ZIP_FILES) == 0:
                continue
                
            tmpFileFormat = allVerFiles[tmpFileKey]
            if isEmpty(tmpFileFormat):
                continue
            tmpFileFormat = tmpFileFormat.replace(HY_VER_REPL_OUT, outPath)
            tmpFileFormat = tmpFileFormat.replace(HY_VER_REPL_PLATFORM, Model.platform)
            tmpFileFormat = tmpFileFormat.replace(HY_VER_REPL_VER_ROOT, Model.dirVerRepl)
            tmpFileFormat = tmpFileFormat.replace(HY_VER_REPL_PROJECT, Model.project)
            tmpFileFormat = tmpFileFormat.replace(HY_VER_REPL_BUILD_SRC, Model.srcPath)
            if tmpFileKey in noZipKeys:
                Model.verFiles.append(tmpFileFormat)
            else:
                Model.verZipFiles.append(tmpFileFormat)
