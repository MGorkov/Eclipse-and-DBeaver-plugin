package ru.tensor.explain.eclipse;

import org.eclipse.datatools.sqltools.core.SQLDevToolsConfiguration;

public class PostgresSQLDevToolsConfiguration extends SQLDevToolsConfiguration {
	private static final String[] PRODUCTS = { "Postgres", "PostgreSQL" };

	public PostgresSQLDevToolsConfiguration() {
		super();
	}

	private String format(String in) {
		return in.trim().toLowerCase();
	}

	@Override
	public boolean recognize(String product, String version) {
		if (product == null) return false;
		
		String formattedProduct = format(product);
		for (int i = 0; i < PRODUCTS.length; i++) {
			if (formattedProduct.indexOf(format(PRODUCTS[i])) > -1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String[] getAssociatedConnectionProfileType() {
		return new String[] { "org.eclipse.datatools.enablement.postgresql.profile.connectionProfile" };
	}


}
