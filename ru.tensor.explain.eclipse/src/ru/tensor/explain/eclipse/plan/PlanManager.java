package ru.tensor.explain.eclipse.plan;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import ru.tensor.explain.eclipse.ExplainPostgreSQLPlugin;
import ru.tensor.explain.eclipse.preferences.PreferenceConstants;
import ru.tensor.explain.eclipse.views.PostgresPlanView;

public class PlanManager implements IPlanManager {
	
	ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	int VIEW_CREATE = 3;
	
	// plan listeners
	ListenerList<IPlanListener> _listeners = new ListenerList<>();

	public PlanManager() {
	}

	@Override
	public void addPlanListener(IPlanListener listener) {
		_listeners.add(listener);
	}

	@Override
	public void removePlanListener(IPlanListener listener) {
		_listeners.remove(listener);
	}

	@Override
	public void firePlan(String plan, String query, int mode) {
		checkView(mode);
		for (IPlanListener listener: this._listeners) {
			listener.planCreated(plan, query);
		}
	}

	@Override
	public void checkView(final int mode) {
    	
    	PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			
			@Override
			public void run() {
		    	IPreferenceStore store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		    	boolean useExternalBrowser = store.getBoolean(PreferenceConstants.P_EXTERNAL);

				// get the active window
		        IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

		        // if can not find the active window, select one from the workbench windows list
		        if (activeWindow == null)
		        {
		            IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		            for (int i = 0; i < windows.length; i++)
		            {
		                activeWindow = windows[i];
		                if (activeWindow != null)
		                {
		                    break;
		                }
		            }
		        }
		        
		        // get the active page in this window
		        IWorkbenchPage activePage = activeWindow.getActivePage();

		        // if can not find the active page, select one from page list
		        if (activePage == null)
		        {
		            IWorkbenchPage[] pages = activeWindow.getPages();
		            for (int i = 0; i < pages.length; i++)
		            {
		                activePage = pages[0];
		                if (activePage != null)
		                {
		                    break;
		                }
		            }
		            if (activePage == null)
		            {
		                return;
		            }
		        }
                try
                {
                	activePage.showView(
                			PostgresPlanView.PLAN_VIEW_ID,
                			null,
                			useExternalBrowser ? VIEW_CREATE : mode
        					);
                }
                catch (PartInitException ex)
                {
                    log.error("PlanManager checkview error", ex);
                }
				
			}
		});
    }

}
