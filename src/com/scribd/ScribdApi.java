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
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.scribd.resource.ScribdUser;

/**
 *
 *
 */
public class ScribdApi implements Api {
	
	public static final String HOST = "api.scribd.com";
	public static final int PORT = 80;
	public static final String REQUEST_PATH = "/api";
	public static final int TRIES = 3;

	private String key;
	private String secret;
	private ScribdUser user;
	private boolean debug = false;

	/**
	 * Uses the following environment variables:
	 *  <ul>
	 *  	<li>SCRIBD_API_KEY</li>
	 *  	<li>SCRIBD_API_SECRET</li>
	 *  	<li>SCRIBD_API_DEBUG</li>
	 * 	</ul>
	 */
	public ScribdApi() {
		this.key = System.getenv("SCRIBD_API_KEY");
		this.secret = System.getenv("SCRIBD_API_SECRET");
		this.debug = "true".equals(System.getenv("SCRIBD_API_DEBUG"));
		this.user = new ScribdUser(this);
	}
	
	/**
	 * Uses the SCRIBD_API_DEBUG environment variable.
	 * @param key API key.
	 * @param secret API secret.
	 */
	public ScribdApi(String key, String secret) {
		this.key = key;
		this.secret = secret;
		this.debug = "true".equals(System.getenv("SCRIBD_API_DEBUG"));
		this.user = new ScribdUser(this);
	}
	
	/**
	 * Authenticate a Scribd user for subsequent API calls.
	 * @param username
	 * @param password
	 */
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

	/**
	 * Performs Scribd API call. For a list of valid method names, see:
	 * http://www.scribd.com/developers/api?method_name=docs.search
	 * @param method The API method name.
	 * @param fields A map of API parameters.
	 */
	@Override
	public Document sendRequest(String method, Map<String, Object> fields) {
		return sendRequest(method, fields, null);
	}

	/**
	 * Performs Scribd API call. If the file parameter is not null, performs an upload.
	 *
	 * For a list of valid method names, see:
	 * http://www.scribd.com/developers/api?method_name=docs.search
	 * @param method The API method name.
	 * @param fields A map of API parameters.
	 * @param file The file to upload.
	 */
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

		includeSession(fields);
		removeNulls(fields);

		fields.put("api_sig", sign(fields));

		BasicHttpParams params = new BasicHttpParams();
		params.setParameter(CookieSpecPNames.DATE_PATTERNS, Arrays.asList("EEE, dd MMM yyyy HH:mm:ss z"));

		HttpClient client = new DefaultHttpClient(params);
		InputStream instream = null;
		try {
			HttpEntity entity = post(fields, file, client);
			if (entity != null) {
				instream = entity.getContent();

				Document doc = read(instream);
				errorCheck(doc, method);

				return doc;
			}
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

	private void includeSession(Map<String, Object> fields) {
		if (!fields.containsKey("session_key") && !fields.containsKey("my_user_id")) {
			if (user.isReal()) {
				fields.put("session_key", user.getSessionKey());
			} else {
				fields.put("my_user_id", user.getMyUserId());
			}
		}
	}

	private void removeNulls(Map<String, Object> fields) {
		Iterator<Object> iter = fields.values().iterator();
		while (iter.hasNext()) {
			if (iter.next() == null) {
				iter.remove();
			}
		}
	}

	private String sign(Map<String, Object> fields) {
		Map<String, Object> signFields = new HashMap<String, Object>(fields);
		signFields.remove("file");

		String[] keys = (String[]) signFields.keySet().toArray(new String[signFields.keySet().size()]);
		Arrays.sort(keys);
		StringBuilder sb = new StringBuilder(secret);
		for (String key : keys) {
			sb.append(key).append(signFields.get(key));
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

	private HttpEntity post(Map<String, Object> fields, File file, HttpClient client) {
		HttpPost post = new HttpPost("http://" + HOST + ":" + PORT + REQUEST_PATH);
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
			return response.getEntity();
		} catch (ClientProtocolException e) {
	        // In case of an unexpected exception you may want to abort
	        // the HTTP request in order to shut down the underlying
	        // connection and release it back to the connection manager.
			post.abort();
	        throw new RuntimeException(e);
		} catch (IOException e) {
	         // In case of an IOException the connection will be released
	         // back to the connection manager automatically
			throw new RuntimeException(e);
		}
	}

	private Document read(InputStream instream) throws IOException, ParserConfigurationException, SAXException {
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

		return doc;
	}

	private void errorCheck(Document doc, String method) {
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
	}
}
