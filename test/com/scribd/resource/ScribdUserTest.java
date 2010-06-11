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

import com.scribd.Scribd;
import com.scribd.resource.ScribdUser;

public class ScribdUserTest {
	private static DocumentBuilder dBuilder = null;
	private static Scribd scribd = null;
	
	@BeforeClass
	public static void initialize() throws ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		scribd = new Scribd();
	}
	
	@Test
	public void initializeFromAttributes() {
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("username", "sancho");
		attributes.put("name", "Sancho Sample");
		ScribdUser user = new ScribdUser(scribd.getApi(), attributes);
		
		assertEquals("sancho", user.getAttribute("username"));
		assertEquals("Sancho Sample", user.getAttribute("name"));
		
		assertFalse(user.isSaved());
		assertFalse(user.isCreated());
	}
	
	@Test
	public void initializeFromXml() {
		try {
			String sampleXml = "<rsp stat='ok'><username>sancho</username><name>Sancho Sample</name></rsp>";
			
			Document doc = dBuilder.parse(new ByteArrayInputStream(sampleXml.getBytes()));
			doc.getDocumentElement().normalize();
			Node xml = doc.getElementsByTagName("rsp").item(0);
	
			ScribdUser user = new ScribdUser(scribd.getApi(), xml);
			
			assertEquals("sancho", user.getAttribute("username"));
			assertEquals("Sancho Sample", user.getAttribute("name"));
			
			assertTrue(user.isSaved());
			assertTrue(user.isCreated());
		} catch (SAXException e) {
			fail();
		} catch (IOException e) {
			fail();
		}
	}
	
	private ScribdUser buildUser() {
		try {
			String sampleXml = "<rsp stat='ok'><user_id type='integer'>225</user_id><username>sancho</username><name>Sancho Sample</name><session_key>some key</session_key></rsp>";
			
			Document doc = dBuilder.parse(new ByteArrayInputStream(sampleXml.getBytes()));
			doc.getDocumentElement().normalize();
			Node xml = doc.getElementsByTagName("rsp").item(0);
	
			return new ScribdUser(scribd.getApi(), xml);
		} catch (SAXException e) {
			fail();
		} catch (IOException e) {
			fail();
		}
		
		return null;
	}
	
	@Test
	public void existingUser() {
		ScribdUser user = buildUser();
		assertEquals(225, user.getId().intValue());
	}
	
	@Test(expected = RuntimeException.class)
	public void saveExistingUser() {
		ScribdUser user = buildUser();
		user.save();
	}
	
}
