package ru.tensor.explain.dbeaver.views;

import java.net.URL;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;

import com.equo.chromium.swt.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.ViewPart;

import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;
import ru.tensor.explain.dbeaver.plan.IPlanListener;
import ru.tensor.explain.dbeaver.preferences.PreferenceConstants;

public class PostgresPlanView extends ViewPart {
	
	private Browser fBrowser;
	private IWebBrowser eBrowser;
	final public static String PLAN_VIEW_ID = "ru.tensor.explain.dbeaver.planView";
	IPreferenceStore store;
	ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	
	IPlanListener _listener = new IPlanListener() {
		
		@Override
		public void planCreated(String plan, String query) {
			setPlan(plan, query);
		}
	};

	public PostgresPlanView() {
		super();
	}

	@Override
	public void createPartControl(Composite parent) {
		store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		fBrowser = new Browser(parent, SWT.NONE);
		fBrowser.setUrl(store.getString(PreferenceConstants.P_SITE));
		ExplainPostgreSQLPlugin.getPlanManager().addPlanListener(_listener);
	}

	@Override
	public void setFocus() {
		
	}

	public void setPlan(String plan, String query) {
		
		Job job = new Job("Explain PostgreSQL explainPlan Thread") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

					@Override
					public void run() {
						
						boolean useExternalBrowser = store.getBoolean(PreferenceConstants.P_EXTERNAL);
						if (useExternalBrowser) {
							if (eBrowser == null) {
								IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
								try {
									eBrowser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, "ru.tensor.expain", "Explain PostgreSQL", "Explain PostgreSQL");
								} catch (PartInitException ex) {
									log.log(new Status(IStatus.ERROR,
												ExplainPostgreSQLPlugin.PLUGIN_ID,
												"createBrowser failed: " + ex.getMessage()
											)
									);
								}
							}
						} else if (eBrowser != null) {
							eBrowser.close();
							eBrowser = null;
						}
												
						try {
							String url = ExplainPostgreSQLPlugin.getExplainAPI().plan_archive(plan, query).join();
							if (eBrowser != null) {
								eBrowser.openURL(new URL(url));
							} else {
								fBrowser.setUrl(url);
							}
						} catch (Exception ex) {
							log.log(new Status(IStatus.ERROR,
										ExplainPostgreSQLPlugin.PLUGIN_ID,
										"Explain failed! Error: " + ex.getMessage()
									)
							);
							MessageDialog.openError(null, "Explain PostgreSQL explainer", ex.getMessage());
						}
					}
				});
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.schedule();
	
	}

	@Override
	public void dispose() {
		ExplainPostgreSQLPlugin.getPlanManager().removePlanListener(_listener);
		if (eBrowser != null) {
			eBrowser.close();
		}
		super.dispose();
	}

}
