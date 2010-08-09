package com.scribd.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.scribd.Api;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ScribdUser extends ScribdResource {
	
	public ScribdUser(Api api) {
		super(api);
	}
	
	public ScribdUser(Api api, Node xml) {
		super(api);
		
		loadAttributes(xml);
		saved = true;
		created = true;
	}
	
	public ScribdUser(Api api, Map<String, Object> attributes) {
		super(api);
		
		setAttributes(attributes);
	}
	
	public Integer getId() {
		return (Integer) getAttribute("user_id");
	}
	
	public boolean isReal() {
		return getAttribute("session_key") != null;
	}
	
	public String getSessionKey() {
		return (String) getAttribute("session_key");
	}
	
	public String getMyUserId() {
		return (String) getAttribute("my_user_id");
	}

	public List<ScribdDocument> getDocuments() {
		return getDocuments(null);
	}
	
	public List<ScribdDocument> getDocuments(Map<String, Object> options) {
		Map<String, Object> fields = (options != null) ? new HashMap<String, Object>(options) : new HashMap<String, Object>();
		fields.put("session_key", getAttribute("session_key"));
		Document xml = api.sendRequest("docs.getList", fields);

		List<ScribdDocument> list = new ArrayList<ScribdDocument>();
		NodeList results = xml.getElementsByTagName("result");
		for (int i = 0; i < results.getLength(); i++) {
			list.add(new ScribdDocument(api, results.item(i)));
		}
		
		return list;
	}
	
	public ScribdDocument findDocument(String documentId) {
		if (!isReal()) {
			return null;
		}
		
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("doc_id", documentId);
		fields.put("session_key", getSessionKey());
		Document xml = api.sendRequest("docs.getSettings", fields);
		Node rsp = xml.getElementsByTagName("rsp").item(0);
		
		return new ScribdDocument(api, rsp);
	}
	
	@Override
	public boolean destroy() {
		throw new NotImplementedException();
	}

	@Override
	public void save() {
		if (!created) {
			Document xml = api.sendRequest("user.signup", getAttributes());
			loadAttributes(xml.getFirstChild());
			api.setUser(this);
		} else {
			throw new IllegalStateException("Cannot update a user once that user's been saved");
		}
	}

	public List<ScribdCollection> getCollections() {
		return getCollections(null);
	}

	public List<ScribdCollection> getCollections(String scope) {
		if (!isReal()) {
			return null;
		}

		Map<String, Object> fields = new HashMap<String, Object>();
		if (scope != null) {
			fields.put("scope", scope);
		}
		fields.put("session_key", getSessionKey());
		Document xml = api.sendRequest("docs.getCollections", fields);

		List<ScribdCollection> list = new ArrayList<ScribdCollection>();
		NodeList results = xml.getElementsByTagName("result");
		for (int i = 0; i < results.getLength(); i++) {
			list.add(new ScribdCollection(api, results.item(i)));
		}

		return list;
	}
}