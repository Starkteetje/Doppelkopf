package com.doppelkopf.java.util;

import java.math.BigDecimal;


public class Functions {
	
	public static String getFloatAsString(float points){
		if(points == 0) {
			return "0";
		}
		return new BigDecimal(Float.toString(points)).stripTrailingZeros().toPlainString();
	}
	
	public static String getBockCountAsString(int bockcount){
		String mStr = "";
        mStr += bockcount;
        mStr += "×";

		return mStr;
	}

}
