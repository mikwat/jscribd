package com.scribd.resource;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

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

	public boolean addDocument(ScribdDocument doc) {
		return addOrRemoveDocument("docs.addToCollection", doc);
	}

	public boolean removeDocument(ScribdDocument doc) {
		return addOrRemoveDocument("docs.removeFromCollection", doc);
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
