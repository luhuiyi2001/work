package com.xy.bean;

import com.xy.common.Const;

public class DailyBasic extends BaseBean {

	public static final String COL_TS_CODE = "ts_code";
	public static final String COL_TRADE_DATE = "trade_date";
	public static final String COL_CLOSE = "close";
	public static final String COL_TURNOVER_RATE = "turnover_rate";
	public static final String COL_TURNOVER_RATE_F = "turnover_rate_f";
	public static final String COL_VOLUME_RATIO = "volume_ratio";
	public static final String COL_PE = "pe";
	public static final String COL_PE_TTM = "pe_ttm";
	public static final String COL_PB = "pb";
	public static final String COL_PS = "ps";
	public static final String COL_PS_TTM = "ps_ttm";
	public static final String COL_TOTAL_SHARE = "total_share";
	public static final String COL_FLOAT_SHARE = "float_share";
	public static final String COL_FREE_SHARE = "free_share";
	public static final String COL_TOTAL_MV = "total_mv";
	public static final String COL_CIRC_MV = "circ_mv";

	private String tsCode;
	private String tradeDate;
	private float close;
	private float turnoverRate;
	private float turnoverRateF;
	private float volumeRatio;
	private float pe;
	private float peTtm;
	private float pb;
	private float ps;
	private float psTtm;
	private float totalShare;
	private float floatShare;
	private float freeShare;
	private float totalMv;
	private float circMv;

	public static DailyBasic createFmDB(String[] colName, String[] rowValue) {
		DailyBasic hkHold = new DailyBasic();
		hkHold.setValue(colName, rowValue);
		return hkHold;
	}

	public static DailyBasic createFmLocal(String content) {
		DailyBasic hkHold = new DailyBasic();
		hkHold.string2Bean(content);
		return hkHold;
	}

	private DailyBasic() {
	}

	public String getTsCode() {
		return tsCode;
	}

	public void setTsCode(String tsCode) {
		this.tsCode = tsCode;
	}

	public String getTradeDate() {
		return tradeDate;
	}

	public void setTradeDate(String tradeDate) {
		this.tradeDate = tradeDate;
	}

	public float getClose() {
		return close;
	}

	public void setClose(float close) {
		this.close = close;
	}

	public float getTurnoverRate() {
		return turnoverRate;
	}

	public void setTurnoverRate(float turnoverRate) {
		this.turnoverRate = turnoverRate;
	}

	public float getTurnoverRateF() {
		return turnoverRateF;
	}

	public void setTurnoverRateF(float turnoverRateF) {
		this.turnoverRateF = turnoverRateF;
	}

	public float getVolumeRatio() {
		return volumeRatio;
	}

	public void setVolumeRatio(float volumeRatio) {
		this.volumeRatio = volumeRatio;
	}

	public float getPe() {
		return pe;
	}

	public void setPe(float pe) {
		this.pe = pe;
	}

	public float getPeTtm() {
		return peTtm;
	}

	public void setPeTtm(float peTtm) {
		this.peTtm = peTtm;
	}

	public float getPb() {
		return pb;
	}

	public void setPb(float pb) {
		this.pb = pb;
	}

	public float getPs() {
		return ps;
	}

	public void setPs(float ps) {
		this.ps = ps;
	}

	public float getPsTtm() {
		return psTtm;
	}

	public void setPsTtm(float psTtm) {
		this.psTtm = psTtm;
	}

	public float getTotalShare() {
		return totalShare;
	}

	public void setTotalShare(float totalShare) {
		this.totalShare = totalShare;
	}

	public float getFloatShare() {
		return floatShare;
	}

	public void setFloatShare(float floatShare) {
		this.floatShare = floatShare;
	}

	public float getFreeShare() {
		return freeShare;
	}

	public void setFreeShare(float freeShare) {
		this.freeShare = freeShare;
	}

	public float getTotalMv() {
		return totalMv;
	}

	public void setTotalMv(float totalMv) {
		this.totalMv = totalMv;
	}

	public float getCircMv() {
		return circMv;
	}

	public void setCircMv(float circMv) {
		this.circMv = circMv;
	}

	private void setValue(String[] colName, String[] rowValue) {
		if (colName == null || rowValue == null || rowValue.length != colName.length) {
			return;
		}
		for (int i = 0; i < colName.length; i++) {
			String curColumn = colName[i];
			String curValue = rowValue[i];
			if (COL_TRADE_DATE.equals(curColumn)) {
				setTradeDate(curValue);
			} else if (COL_TS_CODE.equals(curColumn)) {
				setTsCode(curValue);
			} else if (COL_ID.equals(curColumn)) {
				setId(parseInt(curValue));
			} else if (COL_TOTAL_MV.equals(curColumn)) {
				setTotalMv(parseFloat(curValue));
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
			setTsCode(strArrays[1]);
			setTradeDate(strArrays[2]);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return "DailyBasic{" +
				"tsCode='" + tsCode + '\'' +
				", tradeDate='" + tradeDate + '\'' +
				", close=" + close +
				", turnoverRate=" + turnoverRate +
				", turnoverRateF=" + turnoverRateF +
				", volumeRatio=" + volumeRatio +
				", pe=" + pe +
				", peTtm=" + peTtm +
				", pb=" + pb +
				", ps=" + ps +
				", psTtm=" + psTtm +
				", totalShare=" + totalShare +
				", floatShare=" + floatShare +
				", freeShare=" + freeShare +
				", totalMv=" + totalMv +
				", circMv=" + circMv +
				'}';
	}
}
