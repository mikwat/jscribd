

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.scribd.Scribd;
import com.scribd.resource.ScribdDocument;
import com.scribd.resource.ScribdUser;


public class SimpleTest {
	public static void main(String[] args) {
		Scribd scribd = new Scribd();
		
		System.out.println("===== User =====");
		ScribdUser user = scribd.getApi().login("[username]", "[password]");
		System.out.println(user);
		
		System.out.println("===== User's Docs =====");
		List<ScribdDocument> list = user.getDocuments();
		for (ScribdDocument doc : list) {
			System.out.println(doc);
		}
		
		System.out.println("===== Single Doc =====");
		ScribdDocument findDoc = user.findDocument("21188508");
		System.out.println(findDoc);
		
		System.out.println("===== Upload Doc =====");
		Map<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("access", "private");
		File file = new File("[path-to-file]");
		ScribdDocument uploadDoc = new ScribdDocument(scribd.getApi(), file, user, attributes);
		uploadDoc.setAttribute("title", "Test Title");
		uploadDoc.save();
		System.out.println(uploadDoc);
		
		System.out.println("===== Conversion Status =====");
		System.out.println(uploadDoc.getConversionStatus());
		
		System.out.println("===== Search =====");
		List<ScribdDocument> searchList = scribd.findDocuments("user", "test", 5, 1);
		for (ScribdDocument doc : searchList) {
			System.out.println(doc);
		}
		
		System.out.println("===== Reads =====");
		System.out.println(findDoc.getReads());
		
		System.out.println("===== Delete =====");
		System.out.println(uploadDoc.destroy());
		
		System.out.println("===== Download URL ======");
		System.out.println(findDoc.getDownloadUrl());
	}
}