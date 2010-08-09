package com.scribd.resource;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.scribd.MockApi;
import com.scribd.Scribd;
import com.scribd.resource.ScribdDocument;

public class ScribdDocumentTest {
	private static DocumentBuilder dBuilder = null;
	private static Scribd scribd = null;
	
	@BeforeClass
	public static void initialize() throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		
		System.setProperty("SCRIBD_API_KEY", "test-key");
		System.setProperty("SCRIBD_API_SECRET", "test-sec");
		
		scribd = new Scribd(new MockApi());
	}
	
	@Test
	public void initializeFromAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		
		assertEquals("private", doc.getAttribute("access"));
		assertEquals("mytitle", doc.getAttribute("title"));
		assertFalse(doc.isSaved());
		assertFalse(doc.isCreated());
	}
	
	@Test
	public void initializeFromXml() {
		try {
			String sampleXml = "<rsp stat='ok'><attr1>val1</attr1><attr2>val2</attr2></rsp>";
			
			Document doc = dBuilder.parse(new ByteArrayInputStream(sampleXml.getBytes()));
			doc.getDocumentElement().normalize();
			Node xml = doc.getElementsByTagName("rsp").item(0);
	
			ScribdDocument scribdDoc = new ScribdDocument(scribd.getApi(), xml);
			
			assertEquals("val1", scribdDoc.getAttribute("attr1"));
			assertEquals("val2", scribdDoc.getAttribute("attr2"));
			
			assertTrue(scribdDoc.isSaved());
			assertTrue(scribdDoc.isCreated());
		} catch (SAXException e) {
			fail();
		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void destroy() {
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

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		boolean destroyed = doc.destroy();

		assertEquals(1, mockApi.sendRequestCount);
		assertTrue(destroyed);
	}

	@Test
	public void saveDocument() {
		scribd = new Scribd(new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields, File file) {
				try {
					return dBuilder.parse(new ByteArrayInputStream(
							"<rsp stat=\"ok\"><doc_id>123456</doc_id><access_key>key-rvfa2c82sq5bf9q8t6v</access_key><secret_password>2jzwhplozu43cyqfky1m</secret_password></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		File file = new File("test/testdoc.txt");

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), file, attributes);
		doc.save();

		assertTrue(doc.saved);
	}
	
	@Test(expected = RuntimeException.class)
	public void saveExistingDocument() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		doc.save();
	}

	@Test
	public void getConversionStatus() {
		MockApi mockApi = new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields) {
				try {
					sendRequestCount++;
					return dBuilder.parse(new ByteArrayInputStream(
							"<rsp stat=\"ok\"><conversion_status>PROCESSING</conversion_status></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		scribd = new Scribd(mockApi);

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		String status = doc.getConversionStatus();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals("PROCESSING", status);

		status = doc.getConversionStatus();

		assertEquals(2, mockApi.sendRequestCount);
		assertEquals("PROCESSING", status);
	}

	@Test
	public void getReads() {
		MockApi mockApi = new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields) {
				try {
					sendRequestCount++;
					return dBuilder.parse(new ByteArrayInputStream("<rsp stat=\"ok\"><reads>247</reads></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		scribd = new Scribd(mockApi);

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		int reads = doc.getReads();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals(247, reads);

		reads = doc.getReads();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals(247, reads);
	}

	@Test
	public void getDownloadUrl() {
		MockApi mockApi = new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields) {
				try {
					sendRequestCount++;
					return dBuilder.parse(new ByteArrayInputStream(
							"<rsp stat=\"ok\"><download_link><![CDATA[http://documents.scribd.com.s3.amazonaws.com/docs/dxcu328lru9fpa8.ppt?t=1258673718]]></download_link></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		scribd = new Scribd(mockApi);

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		String downloadUrl = doc.getDownloadUrl();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals("http://documents.scribd.com.s3.amazonaws.com/docs/dxcu328lru9fpa8.ppt?t=1258673718", downloadUrl);

		downloadUrl = doc.getDownloadUrl();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals("http://documents.scribd.com.s3.amazonaws.com/docs/dxcu328lru9fpa8.ppt?t=1258673718", downloadUrl);
	}

	@Test
	public void uploadThumb() {
		scribd = new Scribd(new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields, File file) {
				try {
					return dBuilder.parse(new ByteArrayInputStream("<rsp stat=\"ok\"></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		File file = new File("test/testdoc.txt");

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		boolean uploaded = doc.uploadThumb(file);

		assertTrue(uploaded);
	}

	@Test
	public void uploadThumbFailure() {
		scribd = new Scribd(new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields, File file) {
				try {
					return dBuilder.parse(new ByteArrayInputStream("<rsp stat=\"fail\"><error code=\"601\" message=\"Required parameter missing\"/></rsp>".getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		boolean uploaded = doc.uploadThumb(null);

		assertFalse(uploaded);
	}
}
