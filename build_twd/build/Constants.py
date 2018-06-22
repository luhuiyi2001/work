#!/usr/bin/python
#coding:utf-8

import os

ROOT_PATH=os.path.abspath(os.pardir)
BUILD_PATH= os.getcwd()
TIME_FORMAT = '%Y%m%d%H%M'
DATE_FORMAT = '%y%m%d'

HY_PATH_CONFIG_INI = 'data/conf/config.ini'
HY_DATA = 'data'
HY_CONFIG = 'config'
HY_PATH = 'path'
HY_ZZTEMP = 'ZZTEMP'
HY_TY_PROJECT_CONFIG = 'TyProjectConfig'
HY_DATA = 'data'
HY_CALLING = '通话'
HY_PART_TYPE_SINGLE = '单分区'
HY_PART_TYPE_DOUBLE = '双分区'
HY_PART_TYPE_MTP = 'MTP'
HY_NO_ZIP_FILES = 'no_zip_files'

HY_NG_TYPE_3G = '3G'
HY_NG_TYPE_2G = '2G'
HY_K1001_COM_MODEM_2G = 'K1001_COM_MODEM_2G'

HY_FILE_PROJECT_CONFIG = 'ProjectConfig.mk'
HY_FILE_TY_PROJECT_CONFIG = 'TyProjectConfig.mk'

HY_OP_PATH_VERSION_CONF = 'path_version_conf'
HY_OP_PATH_PLATFORM_CONF = 'path_platform_conf'
HY_OP_PATH_PRODUCT_CONF = 'path_product_conf'
HY_OP_PATH_CUSTOM_CONF = 'path_custom_conf'
HY_OP_PATH_VER_RENAME_CONF = 'path_ver_rename_conf'
HY_OP_PATH_VER_FILE_CONF = 'path_ver_file_conf'
HY_OP_PATH_BOARD_CONF = 'path_board_conf'
HY_OP_PATH_CONF_KEY = 'path_conf_key'

HY_OP_DIR_TMPL_PROJ_CONF = 'dir_tmpl_proj_conf'
HY_OP_DIR_TMPL_ZZTEMP = 'dir_tmpl_zztemp'
HY_OP_DIR_VERSION_REPL = 'dir_version_repl'
HY_OP_DIR_VERSION = 'dir_version'

HY_OP_LIMIT_NG = 'limit_ng'
HY_OP_LIMIT_LCM = 'limit_lcm'
HY_OP_INCLUDE_CUST_CUSTOM = 'include_cust_custom'
HY_OP_DEFAULT_LCM = 'default_lcm'

HY_VERSION_INFO = 'VersionInfo'
HY_VERSION_RENAME = 'VersionRename'
HY_COMMON = 'Common'
HY_IMG = 'IMG'

HY_MAKE_TRUE = 'true'
HY_MAKE_FALSE = 'false'
HY_MK_TYPE_IMAGE = 'IMG'
HY_MK_TYPE_ALL = 'ALL'
HY_MK_TYPE_ARRAY = [HY_MK_TYPE_IMAGE, HY_MK_TYPE_ALL]

HY_PLATFORM_MT6572 = 'MT6572'
HY_PLATFORM_MT6577 = 'MT6577'
HY_PLATFORM_MT6580 = 'MT6580'
HY_PLATFORM_MT6582 = 'MT6582'
HY_PLATFORM_MT6582_L0 = 'MT6582_L0'

HY_CUST_NAME_NEUTRAL = ['TWD_NEUTRAL','TY_NEUTRAL']
HY_OP_MK_TYPE = 'mk_type'
HY_OP_CUSTOM = 'custom'
HY_OP_PRODUCT = 'product'
HY_OP_BOARD = 'board'
HY_OP_CUST_NAME = 'cust_name'
HY_OP_NG = 'ng'
HY_OP_LCM = 'lcm'
HY_OP_SIM = 'sim'
HY_OP_BAND = 'band'
HY_OP_AMP = 'amp'
HY_OP_CUST_COMMON = 'cust_common'
HY_OP_CUST_CUSTOM = 'cust_custom'
HY_OP_VER_FORM = 'ver_form'
HY_OP_MAKE = 'make'

HY_OP_CN_NAME = 'cn_name'

HY_OP_PROJ_CONFIG = 'proj_config'
HY_OP_PROJECT = 'project'
HY_OP_PLATFORM = 'platform'
HY_OP_SRC = 'src'
HY_OP_CUST_DIR = 'cust_dir'
HY_OP_CUST_BOARD_DIR = 'cust_board_dir'
HY_OP_CUST_COMMON_DIR = 'cust_common_dir'
HY_OP_CUST_CUSTOM_DIR = 'cust_custom_dir'
HY_OP_FILE_ZZ_PROJ_CONF = 'zz_proj_conf'

HY_REPL_BOARD = '[HY_BOARD]'
HY_REPL_CUST_NAME = '[HY_CUST_NAME]'
HY_REPL_NG = '[HY_NG]'
HY_REPL_LCM = '[HY_LCM]'
HY_REPL_CUST_COMMON = '[HY_CUST_COMMON]'
HY_REPL_CUST_CUSTOM = '[HY_CUST_CUSTOM]'

HY_VER_REPL_PRODUCT = '[product]'
HY_VER_REPL_BOARD = '[board]'
HY_VER_REPL_NG = '[ng]'
HY_VER_REPL_BAND = '[band]'
HY_VER_REPL_LCM = '[lcm]'
HY_VER_REPL_SIM = '[sim]'
HY_VER_REPL_TIME = '[time]'
HY_VER_REPL_SDCARD = '[sdcard]'
HY_VER_REPL_OUT = '[out]'
HY_VER_REPL_PLATFORM = '[platform]'
HY_VER_REPL_VER_ROOT = '[ver_root]'
HY_VER_REPL_PROJECT = '[project]'
HY_VER_REPL_BUILD_SRC = '[build_src]'