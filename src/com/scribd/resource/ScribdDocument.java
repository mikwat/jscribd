package com.scribd.resource;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.scribd.Api;

public class ScribdDocument extends ScribdResource {
	
	private ScribdUser owner = null;
	private File file = null;
	private int reads = -1;
	private Map<String, String> downloadUrls = new HashMap<String, String>();
	
	public ScribdDocument(Api api, Node xml) {
		this(api, null, xml);
	}
	
	public ScribdDocument(Api api, Map<String, Object> attributes) {
		this(api, null, attributes);
	}
	
	public ScribdDocument(Api api, File file, Map<String, Object> attributes) {
		this(api, file, null, attributes);
	}
	
	public ScribdDocument(Api api, ScribdUser owner, Node xml) {
		super(api);
		this.owner = owner;
		
		loadAttributes(xml);
		saved = true;
		created = true;
	}
	
	public ScribdDocument(Api api, File file, ScribdUser owner, Map<String, Object> attributes) {
		super(api);
		this.owner = owner;
		this.file = file;
		setAttributes(attributes);
	}
	
	@Override
	public boolean destroy() {
		Map<String, Object> fields = new HashMap<String, Object>(1);
		fields.put("doc_id", getAttribute("doc_id"));

		Document xml = api.sendRequest("docs.delete", fields);
		Node rsp = xml.getElementsByTagName("rsp").item(0);
		NamedNodeMap rspAttrs = rsp.getAttributes();
		Node stat = rspAttrs.getNamedItem("stat");
		
		return "ok".equals(stat.getTextContent());
	}

	@Override
	public void save() {
		if (!isCreated() && file == null) {
			throw new IllegalStateException("'file' attribute must be specified for new documents");
		}
		
		if (isCreated() && file != null && (owner == null || owner.getSessionKey() == null)) {
			throw new IllegalStateException();
		}
		
		Map<String, Object> fields = new HashMap<String, Object>(getAttributes());
		if (owner != null) {
			fields.put("session_key", owner.getSessionKey());
		}
		
		if (file != null) {
			String docType = (String) fields.remove("type");
			if (docType == null) {
				String fileName = file.getName().toLowerCase();
				docType = fileName.substring(fileName.lastIndexOf('.') + 1);
			}
			fields.put("doc_type", docType);
			
			Document xml = api.sendRequest("docs.upload", fields, file);
			Node rsp = xml.getElementsByTagName("rsp").item(0);
			
			Map<String, Object> originalAttrs = new HashMap<String, Object>(getAttributes());
			loadAttributes(rsp);
			getAttributes().putAll(originalAttrs);
			created = true;
			
			fields = new HashMap<String, Object>(getAttributes());
		}
		
		fields.put("doc_ids", getAttribute("doc_id"));
		api.sendRequest("docs.changeSettings", fields);
		saved = true;
	}
	
	public String getConversionStatus() {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("doc_id", getAttribute("doc_id"));
		
		Document xml = api.sendRequest("docs.getConversionStatus", fields);
		Node conversionStatus = xml.getElementsByTagName("conversion_status").item(0);
		
		return conversionStatus.getTextContent();
	}
	
	public int getReads() {
		if (this.reads < 0) {
			Map<String, Object> fields = new HashMap<String, Object>(1);
			fields.put("doc_id", getAttribute("doc_id"));
			
			Document xml = api.sendRequest("docs.getStats", fields);
			Node reads = xml.getElementsByTagName("reads").item(0);
			
			this.reads = Integer.parseInt(reads.getTextContent());
		}
		
		return this.reads;
	}
	
	public String getDownloadUrl() {
		return getDownloadUrl("original");
	}
	
	public String getDownloadUrl(String format) {
		String downloadUrl = downloadUrls.get(format);
		if (downloadUrl == null) {
			Map<String, Object> fields = new HashMap<String, Object>();
			fields.put("doc_id", getAttribute("doc_id"));
			fields.put("doc_type", format);
			
			Document xml = api.sendRequest("docs.getDownloadUrl", fields);
			Node downloadLink = xml.getElementsByTagName("download_link").item(0);
		
			downloadUrl = downloadLink.getTextContent();
			downloadUrls.put(format, downloadUrl);
		}
		
		return downloadUrl;
	}
}
