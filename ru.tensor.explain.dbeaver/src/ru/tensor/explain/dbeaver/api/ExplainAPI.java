package ru.tensor.explain.dbeaver.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;
import ru.tensor.explain.dbeaver.preferences.PreferenceConstants;


public class ExplainAPI implements IExplainAPI {
	
	private static final ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	private String EXPLAIN_URL = "https://explain.tensor.ru";
	private static final String API_BEAUTIFIER = "/beautifier-api";
	private static final String API_PLANARCHIVE = "/explain";

	public ExplainAPI() {
	}
	
	private String getExplainURL() {
		IPreferenceStore store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		return store.getString(PreferenceConstants.P_SITE);
	}
	
    @Override
	public CompletableFuture<String> beautifier(String sql) {
    	EXPLAIN_URL = getExplainURL();
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("query_src", sql);
        String json = gson.toJson(stringMap);
        TypeToken<Map<String, String>> mapType = new TypeToken<Map<String, String>>(){};

        log.info("POST JSON: " + json);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create(EXPLAIN_URL + API_BEAUTIFIER))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((res) -> {
                    log.info("Beautifier result: " + res);
                    Map<String, String> resMap = gson.fromJson(res, mapType);
                    return resMap.get("btf_query_text");
                })
                .exceptionally((ex) -> {
                    log.info("Got exception " + ex.getMessage());
                    return "Error: " + ex.getMessage();
                });
    }

    @Override
	public CompletableFuture<String> plan_archive(String plan, String query) {
    	EXPLAIN_URL = getExplainURL();
        Gson gson = new Gson();
        Map<String, String> stringMap = new LinkedHashMap<>();
        stringMap.put("plan", plan);
        stringMap.put("query", query);
        String json = gson.toJson(stringMap);

        log.info("POST JSON: " + json);

        MessageFormat format = new MessageFormat("Found. Redirecting to {0}");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create(EXPLAIN_URL + API_PLANARCHIVE))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
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
                .exceptionally((ex) -> {
                    log.info("Got exception " + ex.getMessage());
                    return EXPLAIN_URL;
                });
    }

}
