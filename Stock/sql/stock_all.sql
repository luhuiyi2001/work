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
