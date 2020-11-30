package achecrawler.target.model;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

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

public class TargetModelCbor {
    
    private static String HOST_NAME;
    private static String HOST_ADDRESS;
    
    static {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            HOST_NAME = localhost.getHostName();
            HOST_ADDRESS = localhost.getHostAddress();
        } catch (UnknownHostException e) {
            HOST_NAME = "localhost";
            HOST_ADDRESS = "127.0.0.1";
        }
    }
    
    public String url;
    public String imported;
    public String key;
    public long timestamp;
    public HashMap<String, Object> request;
    public HashMap<String, Object> response;

    public TargetModelCbor() {
        response = new HashMap<String, Object>();
        request = new HashMap<String, Object>();
        timestamp = System.currentTimeMillis() / 1000L;
    }

    public TargetModelCbor(String contactName, String contactEmail, URL url, String body) {
        this();
        HashMap<String, Object> contact = new HashMap<String, Object>();
        contact.put("name", contactName);
        contact.put("email", contactEmail);

        HashMap<String, Object> client = new HashMap<String, Object>();
        client.put("software", "ACHE");
        client.put("contact", contact);
        client.put("hostname", HOST_NAME);
        client.put("address", HOST_ADDRESS);
        client.put("robots", "classic");
        
        HashMap<String, Object> headers = new HashMap<String, Object>();
        headers.put("Accept-Language", "en-US,en");

        this.request.put("method", "GET");
        this.request.put("client", client);
        this.request.put("headers", headers);
        this.request.put("body", null);
        
        this.response.put("body", body);
        this.url = url.toString();
        this.timestamp = System.currentTimeMillis() / 1000L;
        
        this.key = computeReverseKey(url);
    }
    
    public String computeReverseKey(String url) {
        try {
            return this.computeReverseKey(new URL(url));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL.", e);
        }
    }
    
    public String computeReverseKey(URL url) {
        String urlSha1Hash = DigestUtils.sha1Hex(url.toString());
        String reverseDomain = reverseDomain(url.getHost());
        return reverseDomain + "_" + urlSha1Hash + "_" + timestamp;
    }
    
    private String reverseDomain(String domain) {
        if(domain == null || domain.isEmpty()) {
            return null;
        }
        String[] hostParts = domain.split("\\.");
        if(hostParts.length == 0 ) {
            return null;
        }
        StringBuilder reverseDomain = new StringBuilder();
        reverseDomain.append(hostParts[hostParts.length-1]);
        for (int i = hostParts.length-2; i >= 0; i--) {
            reverseDomain.append('_');
            reverseDomain.append(hostParts[i]);
        }
        return reverseDomain.toString();
    }
    
}
