package ru.tensor.explain.dbeaver;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import ru.tensor.explain.dbeaver.api.ExplainAPI;
import ru.tensor.explain.dbeaver.api.IExplainAPI;
import ru.tensor.explain.dbeaver.plan.IPlanManager;
import ru.tensor.explain.dbeaver.plan.PlanManager;

public class ExplainPostgreSQLPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "Explain PostgreSQL";

	// The shared instance
	private static ExplainPostgreSQLPlugin plugin;
	
	private static int VIEW_ACTIVATE = 1;
	
	private IPlanManager _planManager;
	
	private IExplainAPI _explainAPI;
	
	private static final String BUNDLE_ID = "ru.tensor.explain.dbeaver";
	private static final String platformName = Platform.getProduct().getName() + " " + Platform.getProduct().getDefiningBundle().getVersion().toString();
	private static final Version pluginVersion = Platform.getBundle(BUNDLE_ID).getVersion();
	public static final String versionString = platformName + " " + BUNDLE_ID + "/" + pluginVersion.getMajor() + "." + pluginVersion.getMinor() + "." + pluginVersion.getMicro();
	
	public ExplainPostgreSQLPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ExplainPostgreSQLPlugin getDefault() {
		return plugin;
	}
	
    /**
     * Returns the plan manager of Execution Plan View
     * 
     * @return the plan manager
     */
    public static IPlanManager getPlanManager()
    {
        synchronized (getDefault())
        {
            if (getDefault()._planManager == null)
            {
                getDefault()._planManager = new PlanManager();
            }
            return getDefault()._planManager;
        }
    }

    /**
     * Returns the API service
     * 
     * @return the API service
     */
    public static IExplainAPI getExplainAPI()
    {
        synchronized (getDefault())
        {
            if (getDefault()._explainAPI == null)
            {
                getDefault()._explainAPI = new ExplainAPI();
            }
            return getDefault()._explainAPI;
        }
    }
    
	public static void showView(String viewId) {
    	PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			
			@Override
			public void run() {
				
				IWorkbenchPage activePage = getActivePage();
                try
                {
                	activePage.showView(
                			viewId,
                			null,
                			VIEW_ACTIVATE
        					);
                }
                catch (PartInitException ex)
                {
                    plugin.getLog().error("PlanManager checkview error", ex);
                }
				
			}
		});
	}
	
	public static void hideView(String viewId) {
    	PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			
			@Override
			public void run() {
				IWorkbenchPage activePage = getActivePage();
				IViewPart view = activePage.findView(viewId);
				activePage.hideView(view);			
			}
		});
		
	}
	
	public static IWorkbenchPage getActivePage() {
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
        }
        
        return activePage;

	}


}
