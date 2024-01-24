package ru.tensor.explain.eclipse.api;

import java.util.concurrent.CompletableFuture;

public interface IExplainAPI {

	CompletableFuture<String> beautifier(String sql);

	CompletableFuture<String> plan_archive(String plan, String query);

}