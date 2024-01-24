package ru.tensor.explain.dbeaver;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.tensor.explain.dbeaver.api.ExplainAPI;
import ru.tensor.explain.dbeaver.api.IExplainAPI;
import ru.tensor.explain.dbeaver.plan.IPlanManager;
import ru.tensor.explain.dbeaver.plan.PlanManager;

public class ExplainPostgreSQLPlugin extends AbstractUIPlugin {
	
	// The plug-in ID
	public static final String PLUGIN_ID = "Explain PostgreSQL";

	// The shared instance
	private static ExplainPostgreSQLPlugin plugin;
	
	private IPlanManager _planManager;
	
	private IExplainAPI _explainAPI;
	
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

}
