package com.example.administrator.lc_dvr.common.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import android.util.Log;

/*
 * ������Ҫ�õ�JAR�ļ�  java-json.jar
 * ���Խ�XMLת���ɱ�׼��JSON��ʽ
 * */
public class XmlToJson {
	public static JSONObject convertXml2Json(String xml) {
		JSONObject jsonObj = null;
		try {
			jsonObj = XML.toJSONObject(xml);
		} catch (JSONException e) {
			Log.e("JSON exception", e.getMessage());
			e.printStackTrace();
		}

		// Log.d("XML", xml);
		// Log.d("JSON", jsonObj.toString());
		return jsonObj;
	}
}
