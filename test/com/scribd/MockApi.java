package com.scribd;

import java.io.File;
import java.util.Map;

import org.w3c.dom.Document;

import com.scribd.resource.ScribdUser;

public class MockApi implements Api {

	public int sendRequestCount = 0;

	public MockApi() {
	}

	@Override
	public ScribdUser login(String username, String password) {
		return null;
	}

	@Override
	public Document sendRequest(String method, Map<String, Object> fields) {
		sendRequestCount++;
		return null;
	}

	@Override
	public Document sendRequest(String method, Map<String, Object> fields, File file) {
		sendRequestCount++;
		return null;
	}

	@Override
	public void setUser(ScribdUser user) {
	}
	
	@Override
	public ScribdUser getUser() {
		return null;
	}

}
