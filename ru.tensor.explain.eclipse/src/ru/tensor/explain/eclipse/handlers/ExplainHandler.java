package ru.tensor.explain.eclipse.handlers;

import java.sql.SQLException;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.jkiss.dbeaver.ext.postgresql.model.plan.PostgreQueryPlaner;
import org.jkiss.dbeaver.model.exec.DBCExecutionPurpose;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCSession;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCStatement;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlanner;
import org.jkiss.dbeaver.model.exec.plan.DBCQueryPlannerConfiguration;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.model.sql.SQLScriptElement;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditor;
import org.jkiss.dbeaver.ui.editors.sql.plan.ExplainPlanViewer;
import org.jkiss.dbeaver.utils.GeneralUtils;
import org.jkiss.dbeaver.utils.RuntimeUtils;
import org.jkiss.utils.CommonUtils;

import ru.tensor.explain.eclipse.ExplainPostgreSQLPlugin;

public class ExplainHandler extends AbstractHandler {
	ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	private static Integer PLAN_TASK_TIMEOUT = 30000;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		final ITextEditor editor = (ITextEditor) window.getActivePage().getActiveEditor();
		if (editor == null) {
			return null;
		}
		
		if (editor instanceof SQLEditor) {
			SQLEditor e = (SQLEditor) editor;
	        
			SQLScriptElement scriptElement = e.extractActiveQuery();
	        if (scriptElement != null & scriptElement instanceof SQLQuery) {
		        SQLQuery sqlQuery = (SQLQuery) scriptElement;
		        String sqlQueryText = sqlQuery.getText();
		        final String[] plan = new String[1];
	
				DBCQueryPlanner planner = GeneralUtils.adapt(e.getDataSource(), DBCQueryPlanner.class);
				DBRRunnableWithProgress planObtainTask = monitor -> {
		            DBCQueryPlannerConfiguration configuration = ExplainPlanViewer.makeExplainPlanConfiguration(monitor, planner);
		            if (configuration == null) {
		                return;
		            }
		            try (DBCSession session = e.getExecutionContext().openSession(monitor, DBCExecutionPurpose.UTIL, "Prepare plan query");
							JDBCSession connection = (JDBCSession) session)
		            {
		            	boolean oldAutoCommit = false;
		                try {
		                    oldAutoCommit = connection.getAutoCommit();
		                    if (oldAutoCommit) {
		                        connection.setAutoCommit(false);
		                    }
		                    try (JDBCStatement dbStat = connection.createStatement()) {
		                        JDBCResultSet dbResult = dbStat.executeQuery(getPlanQueryString(configuration) + sqlQuery.toString());
                                String planLines = "";
                                while (dbResult.next()) {
                                    String planLine = dbResult.getString(1);
                                    if (!CommonUtils.isEmpty(planLine)) {
                                        planLines += planLine.toString() + "\n";
                                    }
                                }
                                plan[0] = planLines;
		                    }
		                } catch (SQLException ex) {
		                    ex.printStackTrace();
		                } finally {
		                    // Rollback changes because EXPLAIN actually executes query and it could be INSERT/UPDATE
		                    try {
		                        connection.rollback();
		                        if (oldAutoCommit) {
		                            connection.setAutoCommit(true);
		                        }
		                    } catch (SQLException ex) {
		                        log.error("Error closing plan analyser", ex);
		                    }
		                }

		            } catch (Exception ex) {
		                log.error(ex.toString());
		            }
		        };
		        if (RuntimeUtils.runTask(
		        		planObtainTask,
		        		"Explain '" + sqlQueryText + "'",
		        		PLAN_TASK_TIMEOUT
		        		) && !CommonUtils.isEmpty(plan[0]))
		        {
		    		ExplainPostgreSQLPlugin.getPlanManager().firePlan(
		    				plan[0],
		    				sqlQueryText,
		    				IWorkbenchPage.VIEW_ACTIVATE
		    				);

		        }
	        }
		}
		return null;
		
	}
	
    public String getPlanQueryString(DBCQueryPlannerConfiguration configuration) {
        Map<String, Object> parameters = configuration.getParameters();
        StringBuilder explainStat = new StringBuilder(64);
        explainStat.append("EXPLAIN (FORMAT TEXT");
        for (Map.Entry<String, Object> entry : CommonUtils.safeCollection(parameters.entrySet())) {
            String key = entry.getKey();
            if (PostgreQueryPlaner.PARAM_TIMING.equals(key)
                && !CommonUtils.toBoolean(parameters.get(PostgreQueryPlaner.PARAM_ANALYSE))
            ) {
                continue;
            }
            if (PostgreQueryPlaner.PARAM_COSTS.equals(key) || PostgreQueryPlaner.PARAM_TIMING.equals(key)) {
                if (!CommonUtils.toBoolean(entry.getValue())) {
                    explainStat.append(",").append(key).append(" FALSE");
                }
                continue;
            }
            if (CommonUtils.toBoolean(entry.getValue())) {
                explainStat.append(",").append(key);
            }
        }
        explainStat.append(") ");
        return explainStat.toString();
    }

}
