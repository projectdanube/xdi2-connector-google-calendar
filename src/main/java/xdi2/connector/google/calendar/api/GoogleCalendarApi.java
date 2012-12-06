package xdi2.connector.google.calendar.api;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.xri3.impl.XDI3Segment;

public class GoogleCalendarApi {

	private static final Logger log = LoggerFactory.getLogger(GoogleCalendarApi.class);

	private String appId;
	private String appSecret;
	private HttpClient httpClient;

	public GoogleCalendarApi() {

		this.appId = null;
		this.appSecret = null;
		this.httpClient = new DefaultHttpClient();
	}

	public GoogleCalendarApi(String appId, String appSecret) {

		this.appId = appId;
		this.appSecret = appSecret;
		this.httpClient = new DefaultHttpClient();
	}

	public void init() {

	}

	public void destroy() {

		this.httpClient.getConnectionManager().shutdown();
	}

	// TODO: add methods for talking to API	

	public String startOAuth(HttpServletRequest request, String redirectUri, XDI3Segment userXri) throws IOException {

		// TODO
		
		return null;
	}

	public void checkState(HttpServletRequest request, XDI3Segment userXri) throws IOException {

		// TODO
	}

	public String exchangeCodeForAccessToken(HttpServletRequest request) throws IOException, HttpException {

		// TODO

		return null;
	}

	public void revokeAccessToken(String accessToken) throws IOException, JSONException {

		// TODO
	}

	public JSONObject getUser(String accessToken) throws IOException, JSONException {

		// TODO

		return null;
	}

	public String getAppId() {

		return this.appId;
	}

	public void setAppId(String appId) {

		this.appId = appId;
	}

	public String getAppSecret() {

		return this.appSecret;
	}

	public void setAppSecret(String appSecret) {

		this.appSecret = appSecret;
	}
}
