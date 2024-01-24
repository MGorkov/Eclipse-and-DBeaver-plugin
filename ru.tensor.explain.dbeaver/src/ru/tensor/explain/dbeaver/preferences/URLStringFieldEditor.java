package ru.tensor.explain.dbeaver.preferences;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;

public class URLStringFieldEditor extends StringFieldEditor {
	
	private int validateStrategy = VALIDATE_ON_FOCUS_LOST;

	public URLStringFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
		setEmptyStringAllowed(false);
		setValidateStrategy(validateStrategy);
		setErrorMessage("Please input valid URL");
		
	}

	@Override
	protected boolean doCheckState() {
		String text= getTextControl().getText();
		if (text != null && text.length() > 0) {
			try {
				new URL(text).openStream().close();
			} catch (MalformedURLException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}

}
