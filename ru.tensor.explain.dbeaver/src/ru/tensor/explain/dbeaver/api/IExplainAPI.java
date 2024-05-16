package ru.tensor.explain.dbeaver.api;

import java.util.function.Consumer;

public interface IExplainAPI {

	public void beautifier(String sql, Consumer<String> callback);

	public void plan_archive(String plan, String query, Consumer<String> callback);
	
}