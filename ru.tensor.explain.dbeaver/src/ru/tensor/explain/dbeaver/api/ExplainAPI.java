package ru.tensor.explain.dbeaver.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jface.preference.IPreferenceStore;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("query_src", sql);

        log.info("POST JSON: " + jsonObject);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create(EXPLAIN_URL + API_BEAUTIFIER))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply((res) -> {
                    log.info("Beautifier result: " + res);
                    JsonElement jsonElement = JsonParser.parseString(res);
                    JsonObject object = jsonElement.getAsJsonObject();
                    return object.get("btf_query_text").getAsString();
                })
                .exceptionally((ex) -> {
                    log.info("Got exception " + ex.getMessage());
                    return "Error: " + ex.getMessage();
                });
    }

    @Override
	public CompletableFuture<String> plan_archive(String plan, String query) {
    	EXPLAIN_URL = getExplainURL();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("plan", plan);
        jsonObject.addProperty("query", query);

        log.info("POST JSON: " + jsonObject);

        MessageFormat format = new MessageFormat("Found. Redirecting to {0}");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonObject.toString()))
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
