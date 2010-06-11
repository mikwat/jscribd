package com.scribd.resource;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.scribd.Api;

public abstract class ScribdResource {
	
	protected Api api = null;
	protected boolean saved = false;
	protected boolean created = false;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	
	public abstract void save();
	
	public abstract boolean destroy();
	
	public boolean isSaved() {
		return saved;
	}
	
	public boolean isCreated() {
		return created;
	}
	
	public Map<String, Object> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}
	
	public Object getAttribute(String attr) {
		return attributes.get(attr);
	}
	
	public void setAttribute(String attr, Object val) {
		this.attributes.put(attr, val);
	}
	
	public void loadAttributes(Node xml) {
		attributes.clear();
		NodeList nodeList = xml.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String name = node.getNodeName();
			if ("#text".equals(name)) {
				continue;
			}
			String text = node.getTextContent().trim();
			
			NamedNodeMap attrs = node.getAttributes();
			if (attrs == null) {
				throw new RuntimeException("Expected attributes on node: " + name);
			}
			Node typeAttr = attrs.getNamedItem("type");
			String type = null;
			if (typeAttr != null) {
				type = typeAttr.getNodeValue();
			}
			
			if ("integer".equals(type)) {
				attributes.put(name, Integer.parseInt(text));
			} else if ("float".equals(type)) {
				attributes.put(name, Float.parseFloat(text));
			} else {
				attributes.put(name, text);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getClass().getName()).append("\n");
		for (String key : attributes.keySet()) {
			sb.append(key).append(" => ").append(attributes.get(key)).append("\n");
		}
		return sb.toString();
	}
}
