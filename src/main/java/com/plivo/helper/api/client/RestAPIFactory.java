package com.plivo.helper.api.client;

import org.apache.http.HttpHost;

/**
 * A factory to create new RestAPI's, as the API is automatically closed after each request.
 * 
 * @author dloetscher
 *
 */
public class RestAPIFactory {

	private String		authId;
	private String		authTkn;
	private String		version;
	private HttpHost	proxy	= null;

	public RestAPIFactory(String authId, String authTkn, String version) {
		if (authId == null || authId.isEmpty() || authTkn == null || authTkn.isEmpty() || version == null
				|| version.isEmpty()) {
			throw new IllegalArgumentException("All parameters must be populated. Received: authId: " + authId
					+ ", authTkn: " + authTkn + ", version: " + version);
		}
		this.authId = authId;
		this.authTkn = authTkn;
		this.version = version;
	}

	public RestAPIFactory(String authId, String authTkn, String version, HttpHost proxy) {
		this(authId, authTkn, version);
		setProxy(proxy);
	}

	public void setProxy(HttpHost proxy) {
		if (proxy == null) {
			throw new NullPointerException("Cannot have a null proxy");
		}
		this.proxy = proxy;
	}

	/**
	 * Creates a new RestAPI with the authId, authTkn and version supplied when the class was
	 * created. Also applies a proxy to the API if a proxy has been supplied.
	 * 
	 * @return
	 */
	public RestAPI getAPI() {
		RestAPI api = new RestAPI(authId, authTkn, version);
		if (proxy != null) {
			api.setProxy(proxy);
		}
		return api;
	}

}
