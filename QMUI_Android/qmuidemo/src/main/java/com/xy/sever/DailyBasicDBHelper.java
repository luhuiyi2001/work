package com.xy.sever;

import com.xy.DBManager;
import com.xy.DataTable;
import com.xy.bean.DailyBasic;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;


public class DailyBasicDBHelper {

	public static List<DailyBasic> findByDate(String tradeDate) {
		String[] coulmn = { tradeDate };
		int[] type = { Types.CHAR };
		return find("SELECT * FROM daily_basic WHERE trade_date = ?", coulmn, type);
	}
	
	public static List<DailyBasic> find(String sql, String[] coulmn, int[] type) {
		DBManager dbManager = new DBManager();
		try {
			DataTable dt = dbManager.getResultData(coulmn, type, sql);
			if (dt == null || dt.getRowCount() <= 0) {
				System.out.println("查询失败");
				return null;
			}
			
			List<DailyBasic> DailyBasicList = new ArrayList<DailyBasic>();
			for (int i = 0; i < dt.getRowCount(); i++) {
				DailyBasicList.add(DailyBasic.createFmDB(dt.getColumn(), dt.getRow()[i]));
			}
			return DailyBasicList;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
