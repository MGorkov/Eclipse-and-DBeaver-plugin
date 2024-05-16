package ru.tensor.explain.eclipse.views;

import java.util.function.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.equo.chromium.swt.Browser;

import ru.tensor.explain.eclipse.ExplainPostgreSQLPlugin;
import ru.tensor.explain.eclipse.preferences.PreferenceConstants;

public class ExplainAuthDialog extends Dialog {
	
	@Override
	public void create() {
		super.create();
//		getButton(IDialogConstants.OK_ID).setVisible(false);
//		getButton(IDialogConstants.CANCEL_ID).setVisible(false);
//		getButton(IDialogConstants.CANCEL_ID).setText("Close");
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
//		super.createButtonsForButtonBar(parent);
		GridLayout layout = (GridLayout)parent.getLayout();
		layout.marginHeight = 0;
	}

	private Browser fBrowser;
	IPreferenceStore store;
	ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	private static final String EXPLAIN_TITLE = "Explain PostgreSQL";
	private String title = "";
	private Consumer<String> callback;

	public ExplainAuthDialog(Shell parentShell) {
		this(parentShell, null);
	}
	
	public ExplainAuthDialog(Shell parentShell, Consumer<String> callback) {
		super(parentShell);
		this.callback = callback;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);
		store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		try {
			fBrowser = new Browser(composite, SWT.NONE);
			fBrowser.setUrl(store.getString(PreferenceConstants.P_SITE));
			fBrowser.setLayoutData(new GridData(800, 600));
			TitleListener listener = TitleAdapter.changedAdapter((TitleEvent event) -> {
				System.out.println("TITLE CHANGED: " + event.title);
		        if (!title.equals(event.title)) {
		            title = event.title;
		            if (title.equals(EXPLAIN_TITLE)) {
		                if (callback != null) {
		                    callback.accept("REPEAT");
		                }
		                close();
		            }
		        }
			});
			fBrowser.addTitleListener(listener);
		} catch (Throwable thr) {
			log.log(new Status(IStatus.ERROR,
					ExplainPostgreSQLPlugin.PLUGIN_ID,
					"createBrowser failed: " + thr.getMessage()
				)
			);
			store.setValue(PreferenceConstants.P_EXTERNAL, true);
		}
		return composite;

	}

}
