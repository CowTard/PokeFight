package com.pokefight.oakserver;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResourceResponse {
	private ResourceRequest req;
	
	ResourceResponse(ResourceRequest req) {
		this.req = req;
	}
	
	public JSONObject getResponseObject() throws OakServerException {		
		try {
			Response serverResponse = Request.Get("localhost/api/" + req.getApiPath()).execute();
			if (serverResponse.returnResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
				throw new OakServerException();
			
			return parseObject(serverResponse.returnContent().asString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public JSONArray getResponseArray() throws OakServerException {		
		try {
			Response serverResponse = Request.Get("localhost/api/" + req.getApiPath()).execute();
			if (serverResponse.returnResponse().getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND)
				throw new OakServerException();
			
			return parseArray(serverResponse.returnContent().asString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/* Fonte: https://github.com/mickeyjk/PokeJava/blob/master/src/com/pokejava/ModelClass.java */
	private static JSONObject parseObject(String data) {
		JSONObject root;
		try {
		root = new JSONObject(data);		
		return root;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private static JSONArray parseArray(String data) {
		JSONArray root;
		try {
		root = new JSONArray(data);		
		return root;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
