package com.scribd.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.scribd.Api;

public class ScribdCollection extends ScribdResource {

	private ScribdUser owner = null;

	public ScribdCollection(Api api, Node xml) {
		this(api, null, xml);
	}

	public ScribdCollection(Api api, ScribdUser owner, Node xml) {
		super(api);
		this.owner = owner;

		loadAttributes(xml);
		saved = true;
		created = true;
	}

	public List<ScribdDocument> getDocuments() {
		return getDocuments(null);
	}

	public List<ScribdDocument> getDocuments(Map<String, Object> options) {
		Map<String, Object> fields = (options != null) ? new HashMap<String, Object>(options) : new HashMap<String, Object>();
		fields.put("session_key", getAttribute("session_key"));
		Document xml = api.sendRequest("collections.listDocs", fields);

		List<ScribdDocument> list = new ArrayList<ScribdDocument>();
		NodeList results = xml.getElementsByTagName("result");
		for (int i = 0; i < results.getLength(); i++) {
			list.add(new ScribdDocument(api, results.item(i)));
		}

		return list;
	}

	public boolean addDocument(ScribdDocument doc) {
		return addOrRemoveDocument("collections.addDoc", doc);
	}

	public boolean removeDocument(ScribdDocument doc) {
		return addOrRemoveDocument("collections.removeDoc", doc);
	}

	private boolean addOrRemoveDocument(String method, ScribdDocument doc) {
		Map<String, Object> fields = new HashMap<String, Object>(3);
		if (owner != null) {
			fields.put("session_key", owner.getSessionKey());
		}

		fields.put("doc_id", doc.getAttribute("doc_id"));
		fields.put("collection_id", getAttribute("collection_id"));

		Document xml = api.sendRequest(method, fields);
		Node rsp = xml.getElementsByTagName("rsp").item(0);
		NamedNodeMap rspAttrs = rsp.getAttributes();
		Node stat = rspAttrs.getNamedItem("stat");

		return stat != null && "ok".equals(stat.getTextContent());
	}

	@Override
	public boolean destroy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save() {
		throw new UnsupportedOperationException();
	}

}
