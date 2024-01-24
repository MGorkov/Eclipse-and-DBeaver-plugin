package ru.tensor.explain.dbeaver.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_SITE, "https://explain.tensor.ru");
		store.setDefault(PreferenceConstants.P_EXTERNAL, false);
	}

}
