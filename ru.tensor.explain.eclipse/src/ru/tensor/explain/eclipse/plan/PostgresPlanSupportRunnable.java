package ru.tensor.explain.eclipse.plan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.eclipse.core.runtime.ILog;
import org.eclipse.datatools.sqltools.core.SQLDevToolsConfiguration;
import org.eclipse.datatools.sqltools.core.SQLToolsFacade;
import org.eclipse.datatools.sqltools.core.services.ConnectionService;
import org.eclipse.datatools.sqltools.plan.PlanRequest;
import org.eclipse.datatools.sqltools.plan.PlanSupportRunnable;

import ru.tensor.explain.eclipse.ExplainPostgreSQLPlugin;

public class PostgresPlanSupportRunnable extends PlanSupportRunnable {
	
	private static final ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	
	public PostgresPlanSupportRunnable(PlanRequest request, String profileName, String dbName) {
		super(request, profileName, dbName);
		ExplainPostgreSQLPlugin.getPlanManager().checkView(request.getMode());
	}

	@Override
	protected String explainPlan(Statement stmt) throws SQLException {
		log.info("EXPLAIN (ANALYZE, BUFFERS) " + this._request.getSql());
		
		boolean res = stmt.execute("EXPLAIN (ANALYZE, BUFFERS) " + this._request.getSql());
		if (res) {
			ResultSet rs = stmt.getResultSet();
			String plan = "";
			while (rs.next()) {
				if (plan.length() > 0) {
					plan += '\n';
				}
				plan += rs.getString(1);
			}
			rs.close();
			if (plan.length() == 0) {
				return null;
			}
			
			return plan;
		}
		return null;
	}
	
	
	@Override
	protected void prepareConnection() {
		_conn = createConnection();
	}

	/**
	 * Returns a new created connection from the SQL tools.
	 * 
	 * @return the connection
	 */
	public Connection createConnection() {
		final SQLDevToolsConfiguration config = SQLToolsFacade.getConfigurationByProfileName(_profileName);
		final ConnectionService conService = config.getConnectionService();
		final Connection con = conService.createConnection(_profileName, _dbName);

		_needReleaseConn = true;

		return con;
	}

	@Override
	protected void handleSuccess() {
		super.handleSuccess();
		ExplainPostgreSQLPlugin.getPlanManager().firePlan(
				this._rawPlanString,
				this._request.getSql(),
				this._request.getMode()
				);
	}
	
}
