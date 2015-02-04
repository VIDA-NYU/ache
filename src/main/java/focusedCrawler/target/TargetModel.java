package focusedCrawler.target;
import java.util.HashMap; 


/*
* Proposed schema by IST:
*
*	"url" : "",
*	"timestamp": "",
*	"request": {
*				"method": "",
*				"client": {
*							"hostname": "",
*							"address": "",
*							"software": "",
*							"robots": "",
*							"contact": {
*										"name": "",
*										"email": "",
*										},
*							},
*				"headers": {
*							"Accept": "",
*							"Accept-Encoding": "",
*							"Accept-Language": "",
*							"User-Agent": "",
*							},
*				"body": null,
*				},
*	"response": {
*				"status": "",
*				"server": {
*							"hostname": "",
*							"address": "",
*						  },
*				"headers": {
*							"Content-Encoding": "",
*							"Content-Type": "",
*							"Date": "",
*							"Expires": "",
*							"Server": "",
*							},
*				"body": "",
*				},
*	"key": "",
*	"imported": "",
*/

public class TargetModel {
	public String url;
	public long timestamp;
	public HashMap<String, Object> request;
	public HashMap<String, Object> response;


	
	public TargetModel(String contactName, String contactEmail){
		request = new HashMap<String, Object> ();
		response = new HashMap<String, Object> ();
		
		
		HashMap<String, Object> contact = new HashMap<String, Object> ();
		contact.put("name", contactName);
		contact.put("email", contactEmail);

		HashMap<String, Object> client = new HashMap<String, Object> ();
		client.put("software", "ACHE");
		client.put("contact", contact);

		HashMap<String, Object> headers = new HashMap<String, Object> ();
		headers.put("Accept-Language", "en-US,en");

		request.put("method", "GET");
		request.put("client", client);
		request.put("headers", headers);
		request.put("body", null);
	}

	public void setTimestamp() {
		this.timestamp = System.currentTimeMillis() / 1000L;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setContent(String content) {
		this.response.put("body", content);
	}
}
