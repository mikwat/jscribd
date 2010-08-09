package com.scribd.resource;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

	@Test
	public void getCollections() {
		MockApi mockApi = new MockApi() {
			@Override
			public Document sendRequest(String method, Map<String, Object> fields) {
				try {
					sendRequestCount++;
					return dBuilder.parse(new ByteArrayInputStream(
						("<rsp stat=\"ok\">" +
							"<resultset list=\"true\">" +
								"<result>" +
									"<collection_id>61</collection_id>" +
									"<collection_name>My Collection</collection_name>" +
									"<doc_count>5</doc_count>" +
								"</result>" +
								"<result>" +
									"<collection_id>62</collection_id>" +
									"<collection_name>My Other Collection</collection_name>" +
									"<doc_count>1</doc_count>" +
								"</result>" +
							"</resultset>" +
						"</rsp>").getBytes()));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		scribd = new Scribd(mockApi);

		ScribdUser user = buildUser();
		List<ScribdCollection> list = user.getCollections();

		assertEquals(1, mockApi.sendRequestCount);
		assertEquals(2, list.size());
		assertEquals("61", list.get(0).getAttribute("collection_id"));
		assertEquals("My Collection", list.get(0).getAttribute("collection_name"));
		assertEquals("5", list.get(0).getAttribute("doc_count"));
	}
}
