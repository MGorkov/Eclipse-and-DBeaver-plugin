package ru.tensor.explain.dbeaver.preferences;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

import ru.tensor.explain.dbeaver.views.ExplainAuthDialog;
import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;

public class URLStringFieldEditor extends StringFieldEditor {
	
	private boolean needAuth = false;
	IPreferenceStore store;
	
	@Override
	protected void doStore() {
		super.doStore();
		if (needAuth == true) {
			ExplainAuthDialog authDialog = new ExplainAuthDialog(this.getPage().getShell());
			authDialog.open();
		}
	}

	private int validateStrategy = VALIDATE_ON_FOCUS_LOST;

	public URLStringFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		setEmptyStringAllowed(false);
		setValidateStrategy(validateStrategy);
		setErrorMessage("Please input valid URL");
		store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
	}

	@Override
	protected boolean doCheckState() {
		String text= getTextControl().getText();
		if (text != null && text.length() > 0) {
			try {
				needAuth = false;
				new URL(text).openStream().close();
			} catch (MalformedURLException e) {
				return false;
			} catch (IOException e) {
	            String message = e.getMessage();
	            if (message.contains("401") && !store.getBoolean(PreferenceConstants.P_EXTERNAL)) {
	            	needAuth = true;
					return true;
	            } else {
	                return false;
	            }
			}
		}
		return true;
	}

}
