package com.scribd.resource;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.scribd.MockApi;
import com.scribd.Scribd;

public class ScribdCollectionTest {
	private static DocumentBuilder dBuilder = null;
	private static Scribd scribd = null;
	private static NodeList nodeList = null;

	@BeforeClass
	public static void initialize() throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();

		System.setProperty("SCRIBD_API_KEY", "test-key");
		System.setProperty("SCRIBD_API_SECRET", "test-sec");

		try {
			Document xml = dBuilder.parse(new ByteArrayInputStream(
					("<result>" +
							"<collection_id>61</collection_id>" +
							"<collection_name>My Collection</collection_name>" +
							"<doc_count>5</doc_count>" +
						"</result>").getBytes()));
			nodeList = xml.getElementsByTagName("result");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		MockApi mockApi = new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields) {
				try {
					sendRequestCount++;
					return dBuilder.parse(new ByteArrayInputStream("<rsp stat=\"ok\"></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		scribd = new Scribd(mockApi);
	}

	@Test
	public void initializeFromXml() {
		ScribdCollection collection = new ScribdCollection(scribd.getApi(), nodeList.item(0));

		assertEquals("61", collection.getAttribute("collection_id"));
		assertEquals("My Collection", collection.getAttribute("collection_name"));
		assertEquals("5", collection.getAttribute("doc_count"));
	}

	@Test
	public void addDocument() {
		ScribdCollection collection = new ScribdCollection(scribd.getApi(), nodeList.item(0));

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		boolean added = collection.addDocument(doc);

		assertTrue(added);
	}

	@Test
	public void removeDocument() {
		ScribdCollection collection = new ScribdCollection(scribd.getApi(), nodeList.item(0));

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		boolean added = collection.removeDocument(doc);

		assertTrue(added);
	}
}
