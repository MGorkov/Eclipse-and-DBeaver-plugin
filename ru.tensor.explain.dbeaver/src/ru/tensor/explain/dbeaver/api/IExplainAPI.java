package ru.tensor.explain.dbeaver.api;

import java.util.concurrent.CompletableFuture;

public interface IExplainAPI {

	CompletableFuture<String> beautifier(String sql);

	CompletableFuture<String> plan_archive(String plan, String query);

}