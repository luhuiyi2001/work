package com.xy.bean;

public class BaseBean {
	public static final String COL_ID = "id"; 
	private int mId;

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}
	
	protected int parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	protected float parseFloat(String value) {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
