package com.xy.bean;

import com.xy.common.Const;

public class HkHold extends BaseBean {
	
	public static final String COL_TS_CODE = "ts_code";
	public static final String COL_NAME = "name";
	public static final String COL_CODE = "code";
	public static final String COL_TRADE_DATE = "trade_date";
	public static final String COL_EXCHANGE = "exchange";
	public static final String COL_VOL = "vol";
	public static final String COL_RATIO = "ratio";
	
	private String mTsCode;
	private String mName;
	private String mCode;
	private String mTradeDate;
	private String mExchange;
	private float mVol;
	private float mRatio;
	
	public static HkHold createFmDB(String[] colName, String[] rowValue) {
		HkHold hkHold = new HkHold();
		hkHold.setValue(colName, rowValue);
		return hkHold;
	}
	
	public static HkHold createFmLocal(String content) {
		HkHold hkHold = new HkHold();
		hkHold.string2Bean(content);
		return hkHold;
	}
	
	private HkHold() {
	}
	
	public String getTsCode() {
		return mTsCode;
	}
	public void setTsCode(String tsCode) {
		this.mTsCode = tsCode;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public String getCode() {
		return mCode;
	}
	public void setCode(String code) {
		this.mCode = code;
	}
	public String getTradeDate() {
		return mTradeDate;
	}
	public void setTradeDate(String tradeDate) {
		this.mTradeDate = tradeDate;
	}
	public String getExchange() {
		return mExchange;
	}
	public void setExchange(String exchange) {
		this.mExchange = exchange;
	}
	public float getVol() {
		return mVol;
	}
	public void setVol(float vol) {
		this.mVol = vol;
	}
	public float getRatio() {
		return mRatio;
	}
	public void setRatio(float ratio) {
		this.mRatio = ratio;
	}
	
	private void setValue(String[] colName, String[] rowValue) {
		if (colName == null || rowValue == null || rowValue.length != colName.length) {
			return;
		}
		for (int i = 0; i < colName.length; i++) {
			String curColumn = colName[i];
			String curValue = rowValue[i];
			if (COL_CODE.equals(curColumn)) {
				setCode(curValue);
			} else if (COL_EXCHANGE.equals(curColumn)) {
				setExchange(curValue);
			} else if (COL_NAME.equals(curColumn)) {
				setName(curValue);
			} else if (COL_RATIO.equals(curColumn)) {
				setRatio(parseFloat(curValue));
			} else if (COL_TRADE_DATE.equals(curColumn)) {
				setTradeDate(curValue);
			} else if (COL_TS_CODE.equals(curColumn)) {
				setTsCode(curValue);
			} else if (COL_ID.equals(curColumn)) {
				setId(parseInt(curValue));
			} else if (COL_VOL.equals(curColumn)) {
				setVol(parseFloat(curValue));
			}
		}
	}
	
	public void string2Bean(String content) {
		if (content == null || content.length() == 0) {
			return;
		}
		
		try {
			String[] strArrays = content.split(Const.DOUHAO);
			setId(Integer.parseInt(strArrays[0]));
			setCode(strArrays[1]);
			setTradeDate(strArrays[2]);
			setTsCode(strArrays[3]);
			setName(strArrays[4]);
			setVol(Float.parseFloat(strArrays[5]));
			setRatio(Float.parseFloat(strArrays[6]));
			setExchange(strArrays[7]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "[ " + mTsCode + ", " 
					+ mName + ", " 
					+ mTradeDate + " ]";
	}
}
