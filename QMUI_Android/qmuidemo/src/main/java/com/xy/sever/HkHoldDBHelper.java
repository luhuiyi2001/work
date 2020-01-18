package com.xy.sever;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.xy.DBManager;
import com.xy.DataTable;
import com.xy.bean.HkHold;


public class HkHoldDBHelper {

	public static List<HkHold> findByDate(String exchange, String beginDate, String endDate) {
		if ("SZ".equals(exchange)) {
			return findSZByDate(beginDate, endDate);
		} else if ("SH".equals(exchange)) {
			return findSHByDate(beginDate, endDate);
		} else if ("CY".equals(exchange)) {
			return findCYByDate(beginDate, endDate);
		}
		return findCNByDate(beginDate, endDate);
	}

	private static String getLastDate() {
		List<HkHold>  list = find("select max(trade_date) as trade_date from hk_hold", null, null);
		if (list == null || list.size() == 0) {
			return "";
		}
		return list.get(0).getTradeDate();
	}

	private static List<HkHold> findCYByDate(String beginDate, String endDate) {
		String[] coulmn = { beginDate, endDate };
		int[] type = { Types.CHAR, Types.CHAR };
		return find("SELECT * FROM hk_hold WHERE ts_code like '300%.SZ' AND (trade_date BETWEEN ? AND ?)", coulmn, type);
	}

	private static List<HkHold> findSZByDate(String beginDate, String endDate) {
		String[] coulmn = { beginDate, endDate };
		int[] type = { Types.CHAR, Types.CHAR };
		return find("SELECT * FROM hk_hold WHERE exchange = 'SZ' AND (trade_date BETWEEN ? AND ?)", coulmn, type);
	}

	private static List<HkHold> findSHByDate(String beginDate, String endDate) {
		String[] coulmn = { beginDate, endDate };
		int[] type = { Types.CHAR, Types.CHAR };
		return find("SELECT * FROM hk_hold WHERE exchange = 'SH' AND (trade_date BETWEEN ? AND ?)", coulmn, type);
	}

	private static List<HkHold> findCNByDate(String beginDate, String endDate) {
		String[] coulmn = { beginDate, endDate };
		int[] type = { Types.CHAR, Types.CHAR };
		return find("SELECT * FROM hk_hold WHERE (exchange IN ('SH', 'SZ')) AND (trade_date BETWEEN ? AND ?)", coulmn, type);
	}
	
	public static List<HkHold> find(String sql, String[] coulmn, int[] type) {
		DBManager dbManager = new DBManager();
		try {
			DataTable dt = dbManager.getResultData(coulmn, type, sql);
			if (dt == null || dt.getRowCount() <= 0) {
				System.out.println("查询失败");
				return null;
			}
			
			List<HkHold> hkHoldList = new ArrayList<HkHold>();
			for (int i = 0; i < dt.getRowCount(); i++) {
				hkHoldList.add(HkHold.createFmDB(dt.getColumn(), dt.getRow()[i]));
			}
			return hkHoldList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
