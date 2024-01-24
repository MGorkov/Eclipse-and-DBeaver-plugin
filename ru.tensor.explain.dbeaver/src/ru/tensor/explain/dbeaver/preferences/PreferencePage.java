package ru.tensor.explain.dbeaver.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(ExplainPostgreSQLPlugin.getDefault().getPreferenceStore());
		setDescription("Explain PostgreSQL plugin settings:");
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected void createFieldEditors() {
		
		addField(new URLStringFieldEditor(
				PreferenceConstants.P_SITE,
				"&API URL:",
				getFieldEditorParent()
				));
		addField(new BooleanFieldEditor(
				PreferenceConstants.P_EXTERNAL,
				"&Use external browser",
				getFieldEditorParent()
				));
	}

}
