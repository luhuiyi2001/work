package com.xy.sever;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.utilcode.util.TimeUtils;
import com.google.utilcode.util.ToastUtils;
import com.xy.bean.CountStatic;
import com.xy.bean.HkHold;
import com.xy.common.Const;
import com.xy.common.TRuntime;
import com.xy.google.util.FileIOUtils;
import com.xy.google.util.FileUtils;
import com.xy.util.TUtils;


public class StaticFmSeverData {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log.d("luhuiyi", "----------------------start---------------------");
		long startTime = System.currentTimeMillis();
		
		//loadDataFormServer("20191101", "20191119");
//		doStatic();
		Log.d("luhuiyi", "----------------------over----------------------");
	}
	
	public static void doStatic(String exchange) {
		List<HkHold> allData = readFormLocal(exchange, "20191101", "20191119");
		if (allData == null || allData.size() == 0) {
			Log.d("luhuiyi", "allData is null.");
			return;
		}
		
		Map<String, List<HkHold>> tsDataMap = groupByTsCode(allData);
		if (tsDataMap == null || tsDataMap.size() == 0) {
			Log.d("luhuiyi", "tsDataMap is null.");
			return;
		}
		sortByTsCode(tsDataMap);
		buyTheMostDayNum(tsDataMap);
	}
	
	public static List<CountStatic> buyTheMostDayNum(Map<String, List<HkHold>> tsSortDataMap) {
		Log.d("luhuiyi", "buyTheMostDayNum");
		List<CountStatic> countStaticList = new ArrayList<CountStatic>();
		Set<String> keySet = tsSortDataMap.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		//static
		while (keyIterator.hasNext()) {
			String tsCode = keyIterator.next();
			List<HkHold> tsDataList = tsSortDataMap.get(tsCode);
			int buyNum = 0;
			for (int i = 0 ; i < tsDataList.size() - 1; i++) {
				if (tsDataList.get(i).getVol() > tsDataList.get(i+1).getVol()) {
					buyNum++;
				}
			}
			CountStatic cs = new CountStatic();
			cs.setTsCode(tsCode);
			cs.setName(tsDataList.get(0).getName());
			cs.setCount(buyNum);
			cs.setTotal(tsDataList == null?0:tsDataList.size());
			countStaticList.add(cs);
		}
		//sort
		Collections.sort(countStaticList, new Comparator<CountStatic>() {
			public int compare(CountStatic p1, CountStatic p2) {
				return p2.getCount() - p1.getCount();
			}
		});
		//print
//		for (CountStatic countStatic : countStaticList) {
//			Log.d("luhuiyi", countStatic.toString());
//		}
		return countStaticList;
	}
	
	public static void sortByTsCode(Map<String, List<HkHold>> tsDataMap) {
		Log.d("luhuiyi", "sortByTsCode");
		//每个TS按日期排序
		Set<String> keySet = tsDataMap.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {
			String tsCode = keyIterator.next();
			List<HkHold> tsDataList = tsDataMap.get(tsCode);
			//sort by trade date
			Collections.sort(tsDataList, new Comparator<HkHold>() {
				public int compare(HkHold p1, HkHold p2) {
					return p2.getTradeDate().compareTo(p1.getTradeDate());
				}
			});
		}
	}
	public static Map<String, List<HkHold>> groupByTsCode(List<HkHold> allData) {
		Log.d("luhuiyi", "groupByTsCode");
		if (allData == null || allData.size() == 0) {
			Log.d("luhuiyi", "allData is null.");
			return null;
		}
		//按TS分组
		Map<String, List<HkHold>> groupMap = new HashMap<String, List<HkHold>>();
		for (HkHold bean : allData) {
			String tsCode = bean.getTsCode();
			if (tsCode ==null) {
				continue;
			}
			List<HkHold> tsDataList = groupMap.get(tsCode);
			if (tsDataList == null) {
				tsDataList = new ArrayList<HkHold>();
				groupMap.put(tsCode, tsDataList);
			}
			
			tsDataList.add(bean);
		}
		
		return groupMap;
	}
	
	public static List<HkHold> readFormLocal(String exchange, String startDate, String endDate) {
		Log.d("luhuiyi", "readFormLocal [" + startDate + " - " + endDate + "]");
		String path = Const.STOCK_PATH + File.separator + exchange + "_" + startDate + "-" + endDate;
		if (!FileUtils.isFileExists(path)) {
			Log.d("luhuiyi", path + " isn't exist.");
			return null;
		}
		List<String> lineList = FileIOUtils.readFile2List(path);
		if (lineList == null || lineList.size() == 0) {
			Log.d("luhuiyi", "lineList is null.");
			return null;
		}
		List<HkHold> beanList = new ArrayList<HkHold>();
		for (String content : lineList) {
			HkHold hkHold = HkHold.createFmLocal(content);
			if  (hkHold.getTsCode() != null) {
				beanList.add(HkHold.createFmLocal(content));
			}
		}
		Log.d("luhuiyi", "readFormLocal: " + beanList.size());
		return beanList;
	}
	
	public static void loadDataFormServer() {
		String exchange = TUtils.getExchageTypeName(TRuntime.getExchage());
		String startDate = TimeUtils.millis2String(TRuntime.getStartTime(), TUtils.SDF_DATE);
		String endDate = TimeUtils.millis2String(TRuntime.getEndTime(), TUtils.SDF_DATE);
		Log.d("luhuiyi", "loadDataFormServer:[" + startDate + " - " + endDate + "]");
		String path = Const.STOCK_PATH + File.separator + exchange + "_" + startDate + "-" + endDate;
		if (FileUtils.isFileExists(path)) {
			Log.d("luhuiyi", path + " is exist.");
			ToastUtils.showLong(path + " is exist.");
			return;
		}
		Log.d("luhuiyi",  "path : " + path);
		long startTime = System.currentTimeMillis();
		List<HkHold> hkHoldList = HkHoldDBHelper.findByDate(exchange, startDate, endDate);
		if (hkHoldList == null || hkHoldList.size() == 0) {
			Log.d("luhuiyi", "hkHoldList is null.");
			ToastUtils.showLong("findByDate[" + startDate + " - " + endDate + "] is null!");
			return;
		}
		Log.d("luhuiyi", "hkHoldList size: " + hkHoldList.size());
		String[] contents = new String[hkHoldList.size()];
		for (int i = 0; i < hkHoldList.size(); i++) {
			HkHold bean = hkHoldList.get(i);
			if (bean == null) {
				Log.d("luhuiyi", i + " is null.");
				continue;
			}
			
			StringBuffer sb = new StringBuffer();
			sb.append(bean.getId()).append(Const.DOUHAO);
			sb.append(bean.getCode()).append(Const.DOUHAO);
			sb.append(bean.getTradeDate()).append(Const.DOUHAO);
			sb.append(bean.getTsCode()).append(Const.DOUHAO);
			sb.append(bean.getName()).append(Const.DOUHAO);
			sb.append(bean.getVol()).append(Const.DOUHAO);
			sb.append(bean.getRatio()).append(Const.DOUHAO);
			sb.append(bean.getExchange()).append(Const.LINE);
			contents[i] = sb.toString();
		}
		FileIOUtils.writeFileFromString(path, contents);
		printTime(System.currentTimeMillis(), startTime);
	}

	public static void printTime(long start, long end) {
		Log.d("luhuiyi", "run time: " + ((start - end) / 1000));
	}
	
}
