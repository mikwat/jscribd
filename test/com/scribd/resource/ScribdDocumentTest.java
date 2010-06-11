package com.scribd.resource;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
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
	
	@Test(expected = RuntimeException.class)
	public void saveExistingDocument() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		attributes.put("title", "mytitle");

		ScribdDocument doc = new ScribdDocument(scribd.getApi(), attributes);
		doc.save();
	}
}
