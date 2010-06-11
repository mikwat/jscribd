package com.scribd;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.scribd.resource.ScribdUser;

public class ScribdApi implements Api {
	
	public static final String HOST = "api.scribd.com";
	public static final int PORT = 80;
	public static final String REQUEST_PATH = "/api";
	public static final int TRIES = 3;

	private String key;
	private String secret;
	private ScribdUser user;
	private boolean debug = false;

	public ScribdApi() {
		this.key = System.getenv("SCRIBD_API_KEY");
		this.secret = System.getenv("SCRIBD_API_SECRET");
		this.debug = "true".equals(System.getenv("SCRIBD_API_DEBUG"));
		this.user = new ScribdUser(this);
	}
	
	public ScribdApi(String key, String secret) {
		this.key = key;
		this.secret = secret;
		this.debug = "true".equals(System.getenv("SCRIBD_API_DEBUG"));
		this.user = new ScribdUser(this);
	}
	
	@Override
	public ScribdUser login(String username, String password) {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("username", username);
		fields.put("password", password);
		Document xml = sendRequest("user.login", fields);
		Node rsp = xml.getElementsByTagName("rsp").item(0);
		
		ScribdUser user = new ScribdUser(this, rsp);
		this.user = user;
		
		return user;
	}
	
	@Override
	public void setUser(ScribdUser user) {
		this.user = user;
	}
	
	@Override
	public ScribdUser getUser() {
		return this.user;
	}

	@Override
	public Document sendRequest(String method, Map<String, Object> fields) {
		return sendRequest(method, fields, null);
	}
	
	@Override
	public Document sendRequest(String method, Map<String, Object> fields, File file) {
		if (key == null || secret == null) {
			throw new NotReadyException();
		}

		if (method == null) {
			throw new NullPointerException("Method should be given");
		}

		fields.put("method", method);
		fields.put("api_key", key);
		
		if (!fields.containsKey("session_key") && !fields.containsKey("my_user_id")) {
			if (user.isReal()) {
				fields.put("session_key", user.getSessionKey());
			} else {
				fields.put("my_user_id", user.getMyUserId());
			}
		}
		
		for (String key : fields.keySet()) {
			if (fields.get(key) == null) {
				fields.remove(key);
			}
		}

		Map<String, Object> signFields = new HashMap<String, Object>(fields);
		signFields.remove("file");

		fields.put("api_sig", sign(signFields));

		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://" + HOST + ":" + PORT + REQUEST_PATH);
		InputStream instream = null;
		try {
			MultipartEntity multipartEntity = new MultipartEntity();
			for (String key : fields.keySet()) {
				multipartEntity.addPart(key, new StringBody(fields.get(key).toString()));
			}
			if (file != null) {
				multipartEntity.addPart("file", new FileBody(file, "application/octet-stream"));
			}
			post.setEntity(multipartEntity);
			
			HttpResponse response = client.execute(post);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				instream = entity.getContent();
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					if (debug) {
						System.out.println(line);
					}
				}
				
				InputStream stringInputStream = new ByteArrayInputStream(sb.toString().getBytes("UTF-8"));
				
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(stringInputStream);
				doc.getDocumentElement().normalize();
				
				NodeList nodes = doc.getElementsByTagName("rsp");
				if (nodes.getLength() == 0) {
					throw new RuntimeException("The response received from the remote host could not be interpreted");
				}
				
				Node rsp = nodes.item(0);
				NamedNodeMap rspAttrs = rsp.getAttributes();
				Node stat = rspAttrs.getNamedItem("stat");
				if (stat == null || "fail".equals(stat.getNodeValue())) {
					String code = "-1";
					String message = "Unidentified error.";
					
					NodeList error = doc.getElementsByTagName("error");
					if (error.getLength() > 0) {
						NamedNodeMap errorAttrs = error.item(0).getAttributes();
						code = errorAttrs.getNamedItem("code").getNodeValue();
						message = errorAttrs.getNamedItem("message").getNodeValue();
					}
					
					message = "Method: " + method + " Response: code=" + code + " message=" + message;
					
					throw new RuntimeException(message);
				}
				
				return doc;
			}
		} catch (RuntimeException e) {
	         // In case of an unexpected exception you may want to abort
	         // the HTTP request in order to shut down the underlying 
	         // connection and release it back to the connection manager.
	         post.abort();
	         throw e;
		} catch (IOException e) {
	         // In case of an IOException the connection will be released
	         // back to the connection manager automatically
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		} finally {
			// Closing the input stream will trigger connection release
	        if (instream != null) {
	        	try {
	        		instream.close();
				} catch (IOException e) {
				}
	        }

		    // When HttpClient instance is no longer needed, 
		    // shut down the connection manager to ensure
		    // immediate deallocation of all system resources
	        client.getConnectionManager().shutdown();
	    }
		
		return null;
	}

	private String sign(Map<String, Object> fields) {
		String[] keys = (String[]) fields.keySet().toArray(new String[fields.keySet().size()]);
		Arrays.sort(keys);
		StringBuilder sb = new StringBuilder(secret);
		for (String key : keys) {
			sb.append(key).append(fields.get(key));
		}
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(sb.toString().getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			return bigInt.toString(16);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
