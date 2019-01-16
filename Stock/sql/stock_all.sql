/*
Navicat MySQL Data Transfer

Source Server         : 127.0.0.1
Source Server Version : 50614
Source Host           : localhost:3306
Source Database       : stock

Target Server Type    : MYSQL
Target Server Version : 50614
File Encoding         : 65001

Date: 2014-11-21 10:31:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `account_user`
-- ----------------------------
DROP TABLE IF EXISTS `stock_all`;
CREATE TABLE `stock_all` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `state_dt` VARCHAR(45) NOT NULL COMMENT '交易日',
  `stock_code` VARCHAR(45) NOT NULL COMMENT '股票代码',
  `open` DECIMAL(20,2) unsigned NOT NULL COMMENT '开盘价',
  `close` DECIMAL(20,2) unsigned NOT NULL COMMENT '收盘价',
  `high` DECIMAL(20,2) unsigned NOT NULL COMMENT '最高价',
  `low` DECIMAL(20,2) unsigned NOT NULL COMMENT '最低价',
  `vol` INT(20) unsigned NOT NULL COMMENT '成交量',
  `amount` DECIMAL(20,2) unsigned NOT NULL COMMENT '成交额',
  `pre_close` DECIMAL(20,2) unsigned DEFAULT 0 COMMENT '前日收盘价',
  `amt_change` DECIMAL(20,2)  NOT NULL COMMENT '涨跌额',
  `pct_change` DECIMAL(20,2)  NOT NULL COMMENT '涨跌幅',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

/*
股票列表
接口：stock_basic
描述：获取基础信息数据，包括股票代码、名称、上市日期、退市日期等
*/
DROP TABLE IF EXISTS `stock_basic`;
CREATE TABLE `stock_basic` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `ts_code` VARCHAR(45) NOT NULL COMMENT 'TS代码',
  `symbol` VARCHAR(45) NOT NULL COMMENT '股票代码',
  `name` VARCHAR(45) COMMENT '股票名称',
  `area` VARCHAR(45) COMMENT '所在地域',
  `industry` VARCHAR(45) COMMENT '所属行业',
  `fullname` VARCHAR(200) COMMENT '股票全称',
  `enname` VARCHAR(200) COMMENT '英文全称',
  `market` VARCHAR(45) COMMENT '市场类型(主板/中小板/创业板)',
  `exchange` VARCHAR(45) COMMENT '交易所代码',
  `curr_type` VARCHAR(45) COMMENT '交易货币',
  `list_status` VARCHAR(45) COMMENT '上市状态:L上市 D退市 P暂停上市',
  `list_date` VARCHAR(45) COMMENT '上市日期',
  `delist_date` VARCHAR(45) COMMENT '退市日期',
  `is_hs` VARCHAR(45) NOT NULL DEFAULT 'N' COMMENT '是否沪深港通标: N否 H沪股通 S深股通',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
