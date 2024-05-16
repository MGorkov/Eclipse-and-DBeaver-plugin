package ru.tensor.explain.eclipse.views;

import java.util.function.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.ui.part.ViewPart;

import com.equo.chromium.swt.Browser;

import ru.tensor.explain.eclipse.ExplainPostgreSQLPlugin;
import ru.tensor.explain.eclipse.preferences.PreferenceConstants;

public class AuthView extends ViewPart {
	
	private Browser fBrowser;
	final public static String AUTH_VIEW_ID = "ru.tensor.explain.authView";
	IPreferenceStore store;
	ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	private static final String EXPLAIN_TITLE = "Explain PostgreSQL";
	private String title = "";
	Consumer<String> callback;

	public AuthView() {
		super();
	}
	
	public void setCallback(Consumer<String> callback) {
		this.callback = callback;
	}

	@Override
	public void createPartControl(Composite parent) {
		store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		try {
			fBrowser = new Browser(parent, SWT.NONE);
			fBrowser.setUrl(store.getString(PreferenceConstants.P_SITE));
			TitleListener listener = TitleAdapter.changedAdapter((TitleEvent event) -> {
				System.out.println("TITLE CHANGED: " + event.title);
		        if (!title.equals(event.title)) {
		            title = event.title;
		            if (title.equals(EXPLAIN_TITLE)) {
		                if (callback != null) {
		                	callback = null;
		                    callback.accept("REPEAT");
		                }
		                ExplainPostgreSQLPlugin.hideView(AUTH_VIEW_ID);
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
		}

	}

	@Override
	public void dispose() {
		if (fBrowser != null) {
			fBrowser.dispose();
		}
		super.dispose();
	}

	@Override
	public void setFocus() {
		
	}
	
}

abstract class TitleAdapter implements TitleListener {

	public static TitleListener changedAdapter(Consumer<TitleEvent> cb) {
		return new TitleAdapter() {
			@Override
			public void changed(TitleEvent e) {
				cb.accept(e);
			}
		};
	}

	@Override
	public void changed(TitleEvent event) {
		
	}

}

