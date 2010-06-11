package com.scribd;

import java.io.File;
import java.util.Map;

import org.w3c.dom.Document;

import com.scribd.resource.ScribdUser;

public interface Api {
	public Document sendRequest(String method, Map<String, Object> fields);
	
	public Document sendRequest(String method, Map<String, Object> fields, File file);
	
	public ScribdUser login(String username, String password);
	
	public void setUser(ScribdUser user);
	
	public ScribdUser getUser();
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
