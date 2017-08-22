package focusedCrawler.crawler.async.cookieHandler;

import java.io.Serializable;

public class Cookie implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8502369293267383776L;

	private String name;
	private String value;
	private long expiresAt;
	private String domain;
	private String path;
	private boolean secure;
	private boolean httpOnly;

	private boolean persistent;
	private boolean hostOnly;

	// Added for serialization
	public Cookie() {

	}

	public Cookie(String name, String value) {
		this.setName(name);
		this.setValue(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isHttpOnly() {
		return httpOnly;
	}

	public void setHttpOnly(boolean httpOnly) {
		this.httpOnly = httpOnly;
	}

	public boolean isPersistent() {
		return persistent;
	}

	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	public boolean isHostOnly() {
		return hostOnly;
	}

	public void setHostOnly(boolean hostOnly) {
		this.hostOnly = hostOnly;
	}

	public String toString() {
		return "Name: " + name + ", Value: " + value + ", Domain: " + domain + ", Path: " + path;
	}

}
