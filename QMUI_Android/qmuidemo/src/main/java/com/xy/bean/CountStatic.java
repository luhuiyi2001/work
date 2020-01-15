package com.xy.bean;


public class CountStatic {
	private String mTsCode;
	private String mName;
	private int mCount;
	private int mTotal;
	
	public CountStatic() {
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
	
	public int getCount() {
		return mCount;
	}

	public void setCount(int count) {
		this.mCount = count;
	}
	
	public int getTotal() {
		return mTotal;
	}

	public void setTotal(int total) {
		this.mTotal = total;
	}
	
	@Override
	public String toString() {
		return "[ " + mTsCode + ", " 
					+ mName + ", " 
					+ mCount + "/" 
					+ mTotal + " ]";
	}
	
}
