package com.scribd;

import java.io.File;
import java.util.Map;

import org.w3c.dom.Document;

import com.scribd.resource.ScribdUser;

public interface Api {
	Document sendRequest(String method, Map<String, Object> fields);
	
	Document sendRequest(String method, Map<String, Object> fields, File file);
	
	ScribdUser login(String username, String password);
	
	void setUser(ScribdUser user);
	
	ScribdUser getUser();
}

class NotReadyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	NotReadyException() {
		super();
	}
	
	NotReadyException(String msg) {
		super(msg);
	}
}
