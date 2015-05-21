package focusedCrawler.target;

import java.util.HashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

public class TargetModel {
    
    public String url;
    public String imported;
    public String key;
    public long timestamp;
    public HashMap<String, Object> request;
    public HashMap<String, Object> response;

    public TargetModel() {
    }

    public TargetModel(String contactName, String contactEmail) {
        request = new HashMap<String, Object>();
        response = new HashMap<String, Object>();

        HashMap<String, Object> contact = new HashMap<String, Object>();
        contact.put("name", contactName);
        contact.put("email", contactEmail);

        HashMap<String, Object> client = new HashMap<String, Object>();
        client.put("software", "ACHE");
        client.put("contact", contact);

        HashMap<String, Object> headers = new HashMap<String, Object>();
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
        this.response.put("body", content.replaceAll("(\r?\n)", " "));
    }

    public void setKey(String url, String domain) throws NoSuchAlgorithmException {
        String sb = computeSHA1(url);
        this.key = domain + "-" + sb;
    }
    
    public void setReverseKey(String url, String domain) {
        String urlSha1Hash = DigestUtils.sha1Hex(url);
        String reverseDomain = reverseDomain(domain);
        this.key = reverseDomain + "_" + urlSha1Hash + "_" + timestamp;
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

    // FIXME Remove this algorithm, it seems to be wrong.
    // Better to use Apache Commons's DigestUtils.sha1Hex()
    @Deprecated
    private String computeSHA1(String url) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(url.getBytes());
        byte byteData[] = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
    
}
