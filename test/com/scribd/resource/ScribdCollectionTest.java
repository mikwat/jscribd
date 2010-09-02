package com.scribd.resource;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
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

	@Test
	public void getDocuments() {
		MockApi mockApi = new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields) {
				try {
					sendRequestCount++;
					return dBuilder.parse(new ByteArrayInputStream(
						("<rsp stat=\"ok\">" +
							"<result_set totalResultsAvailable=\"922\" totalResultsReturned=\"2\" firstResultPosition=\"1\" list=\"true\">" +
								"<result>" +
									"<title><![CDATA[Ruby on Java]]></title>" +
									"<description><![CDATA[Ruby On Java, Barcamp, Washington DC]]></description>" +
									"<access_key>key-t3q5qujoj525yun8gf7</access_key>" +
									"<doc_id>244565</doc_id>" +
									"<page_count>10</page_count>" +
									"<download_formats></download_formats>" +
									"<when_uploaded>2009-09-02T18:24:12-05:00</when_uploaded>" +
									"<when_updated>2010-08-02T18:24:12-05:00</when_updated>" +
								"</result>" +
								"<result>" +
									"<title><![CDATA[Ruby on Java Part II]]></title>" +
									"<description><![CDATA[Ruby On Java Part II, Barcamp, Washington DC]]></description>" +
									"<access_key>key-2b3udhalycthsm91d1ps</access_key>" +
									"<doc_id>244567</doc_id>" +
									"<page_count>12</page_count>" +
									"<download_formats>pdf,txt</download_formats>" +
								"</result>" +
							"</result_set>" +
						"</rsp>").getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		scribd = new Scribd(mockApi);

		ScribdCollection collection = new ScribdCollection(scribd.getApi(), nodeList.item(0));
		List<ScribdDocument> list = collection.getDocuments();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals(2, list.size());
		assertEquals("244565", list.get(0).getAttribute("doc_id"));
		assertEquals("Ruby on Java", list.get(0).getAttribute("title"));
		assertEquals("10", list.get(0).getAttribute("page_count"));
	}
}
