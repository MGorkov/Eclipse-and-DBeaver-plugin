package ru.tensor.explain.dbeaver.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

import com.equo.chromium.ChromiumBrowser;

import org.cef.callback.CefAuthCallback;
import org.cef.callback.CefURLRequestClient;
import org.cef.network.CefPostData;
import org.cef.network.CefPostDataElement;
import org.cef.network.CefRequest;
import org.cef.network.CefResponse;
import org.cef.network.CefURLRequest;
import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;
import ru.tensor.explain.dbeaver.preferences.PreferenceConstants;
import ru.tensor.explain.dbeaver.views.ExplainAuthDialog;

public class ExplainAPI implements IExplainAPI {
	
	private static final ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	private String EXPLAIN_URL = "https://explain.tensor.ru";
	private static final String API_BEAUTIFIER = "/beautifier-api";
	private static final String API_PLANARCHIVE = "/explain";
	private IPreferenceStore store;

	public ExplainAPI() {
		store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		try {
			ChromiumBrowser.earlyInit();
			ChromiumBrowser.windowless(getExplainURL());
		} catch (Throwable e) {
			log.error("API: cannot init chromium browser: " + e.getMessage());
			store.setValue(PreferenceConstants.P_EXTERNAL, true);
		}
	}
	
	private String getExplainURL() {
		return store.getString(PreferenceConstants.P_SITE);
	}
	
    @Override
	public void beautifier(String sql, Consumer<String> callback) {
    	EXPLAIN_URL = getExplainURL();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("query_src", sql);

        log.info("POST JSON: " + jsonObject);
        
        if (store.getBoolean(PreferenceConstants.P_EXTERNAL)) {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .header("content-type", "application/json")
                    .header("user-agent", ExplainPostgreSQLPlugin.versionString)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                    .timeout(Duration.ofSeconds(30))
                    .uri(URI.create(EXPLAIN_URL + API_BEAUTIFIER))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply((String res) -> {
                        log.info("Beautifier result: " + res);
                        JsonElement jsonElement = JsonParser.parseString(res);
                        JsonObject object = jsonElement.getAsJsonObject();
                        return object.get("btf_query_text").getAsString();
                    })
                    .handle((result, ex) -> {
                    	if (ex != null) {
                    		return "Error: " + ex.getMessage();
                    	} else {
	                        return result;
                    	}
                    })
                    .thenAccept(callback::accept);
        } else {
	        request(EXPLAIN_URL + API_BEAUTIFIER, jsonObject.toString(), (String data) -> {
	        	if (data.equals("REPEAT")) {
	        		beautifier(sql, callback);
	        	} else {
		        	try {
		                log.info("Beautifier result: " + data);
		                JsonElement jsonElement = JsonParser.parseString(data);
		                JsonObject object = jsonElement.getAsJsonObject();
		                callback.accept(object.get("btf_query_text").getAsString());
		        	} catch (Exception ex) {
		                log.info("Got exception " + ex.getMessage());
		                callback.accept("Error: " + ex.getMessage());
		        	}
	        	}
	        });
        }

    }

    @Override
	public void plan_archive(String plan, String query, Consumer<String> callback) {
    	EXPLAIN_URL = getExplainURL();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("plan", plan);
        jsonObject.addProperty("query", query);

        log.info("POST JSON: " + jsonObject);

        if (store.getBoolean(PreferenceConstants.P_EXTERNAL)) {
            MessageFormat format = new MessageFormat("Found. Redirecting to {0}");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .header("content-type", "application/json")
                    .header("user-agent", ExplainPostgreSQLPlugin.versionString)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                    .timeout(Duration.ofSeconds(30))
                    .uri(URI.create(EXPLAIN_URL + API_PLANARCHIVE))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply((res) -> {
                        if (res == null) {
                            log.info("Received null result, going to " + EXPLAIN_URL);
                            return EXPLAIN_URL;
                        } else {
                            log.info("Received result: " + res);
                            try {
                                Object[] parse = format.parse(res);
                                log.info("Parsed result: " + parse[0]);
                                return (EXPLAIN_URL + parse[0].toString());
                            } catch (ParseException e) {
                                log.info("Parse error: " + e.getMessage());
                                return EXPLAIN_URL;
                            }
                        }
                    })
                    .handle((result, ex) -> {
                    	if (ex != null) {
                    		log.info("Error: " + ex.getMessage());
                    		return EXPLAIN_URL;
                    	} else {
	                        return result;
                    	}
                    })
                    .thenAccept(callback::accept);

        } else {
	        request(EXPLAIN_URL + API_PLANARCHIVE, jsonObject.toString(), (String data) -> {
	            log.info("Received result: " + data);
	            if (data.equals("REPEAT")) {
	            	plan_archive(plan, query, callback);
	            } else {
	            	callback.accept(EXPLAIN_URL + data);
	            }
	        });
        }
    }
    
	public void request(String URL, String json, Consumer<String> callback) {
		CefRequest cefRequest = CefRequest.create();
		CefPostData cefPostData = CefPostData.create();
		CefPostDataElement cefPostDataElement = CefPostDataElement.create();
		byte[] bytes = json.getBytes();
		cefPostDataElement.setToBytes(bytes.length, bytes);
		cefPostData.addElement(cefPostDataElement);
		Map<String, String> headers = Map.of(
				"Content-Type", "application/json",
				"User-Agent", ExplainPostgreSQLPlugin.versionString
				);
		cefRequest.setMethod("POST");
		cefRequest.setURL(URL);
		cefRequest.setHeaderMap(headers);
		cefRequest.setPostData(cefPostData);
		cefRequest.setFlags(1 << 3 | 1 << 7); // UR_FLAG_ALLOW_STORED_CREDENTIALS | UR_FLAG_STOP_ON_REDIRECT
		cefRequest.setFirstPartyForCookies(getExplainURL());
		APIClient client = new APIClient(callback);
		CefURLRequest.create(cefRequest, client);
	}


}

class APIClient implements CefURLRequestClient {

	public String data = "";
	Consumer<String> callback;

	public APIClient(Consumer<String> callback) {
		this.callback = callback;
	}

	@Override
	public void onRequestComplete(CefURLRequest cefURLRequest) {
		CefResponse response = cefURLRequest.getResponse();
		int responseStatus = response.getStatus();
		if (responseStatus == 200 && data.length() > 0) {
			this.callback.accept(data);
		} else if (responseStatus == 302) {
			this.callback.accept(response.getHeaderByName("Location"));
		} else if (responseStatus == 401) {
	    	PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					
					Shell shell = ExplainPostgreSQLPlugin.getActivePage().getWorkbenchWindow().getShell();
					ExplainAuthDialog authDialog = new ExplainAuthDialog(shell, callback);
					authDialog.open();
					
				}
			});

		}
	}

	@Override
	public void onUploadProgress(CefURLRequest cefURLRequest, int i, int i1) {

	}

	@Override
	public void onDownloadProgress(CefURLRequest cefURLRequest, int i, int i1) {

	}

	@Override
	public void onDownloadData(CefURLRequest cefURLRequest, byte[] bytes, int i) {
		data += new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public boolean getAuthCredentials(boolean b, String s, int i, String s1, String s2, CefAuthCallback cefAuthCallback) {
		return false;
	}

	@Override
	public void setNativeRef(String s, long l) {

	}

	@Override
	public long getNativeRef(String s) {
		return 0;
	}
}

