package com.scribd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.scribd.resource.ScribdDocument;

/**
 *
 *
 */
public class Scribd {
	
	private static final int DEFAULT_SEARCH_LIMIT = 10;
	private static final int DEFAULT_SEARCH_OFFSET = 1;

	private final Api api;
	
	public Scribd() {
		api = new ScribdApi();
	}
	
	public Scribd(Api api) {
		this.api = api;
	}
	
	public Api getApi() {
		return api;
	}
	
	/**
	 * For more information, see the Scribd API documentation:
	 * http://www.scribd.com/developers/api?method_name=docs.search
	 *
	 * @param scope The search scope.
	 * @param query The search query.
	 * @return A list of documents matching the search parameters.
	 */
	public List<ScribdDocument> findDocuments(String scope, String query) {
		return findDocuments(scope, query, DEFAULT_SEARCH_LIMIT, DEFAULT_SEARCH_OFFSET);
	}

	/**
	 * For more information, see the Scribd API documentation:
	 * http://www.scribd.com/developers/api?method_name=docs.search
	 *
	 * @param scope The search scope.
	 * @param query The search query.
	 * @param limit The maximum number of results to return. Translates to the "num_results" API parameter.
	 * @param offset The search result offset. Translates to the "num_start" API parameter.
	 * @return A list of documents matching the search parameters.
	 */
	public List<ScribdDocument> findDocuments(String scope, String query, int limit, int offset) {
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("scope", scope);
		fields.put("query", query);
		fields.put("num_results", limit);
		fields.put("num_start", offset);

		Document xml = api.sendRequest("docs.search", fields);
		
		List<ScribdDocument> list = new ArrayList<ScribdDocument>();
		NodeList results = xml.getElementsByTagName("result");
		for (int i = 0; i < results.getLength(); i++) {
			list.add(new ScribdDocument(api, results.item(i)));
		}
		
		return list;
	}
}
