package ru.tensor.explain.dbeaver.plan;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.jkiss.dbeaver.model.exec.plan.DBCPlan;
import org.jkiss.dbeaver.model.sql.SQLQuery;
import org.jkiss.dbeaver.ui.editors.sql.SQLPlanSaveProvider;

public class PlanProvider extends SQLPlanSaveProvider {

	public PlanProvider() {
		super();
	}

	@Override
	public void contributeActions(Viewer viewer, IContributionManager contributionManager, SQLQuery lastQuery,
			DBCPlan lastPlan) {
		super.contributeActions(viewer, contributionManager, lastQuery, lastPlan);
	}

	@Override
	public Viewer createPlanViewer(IWorkbenchPart workbenchPart, Composite parent) {
		PlanViewer planViewer = new PlanViewer(workbenchPart, parent);
		return planViewer;
	}

	@Override
	public void visualizeQueryPlan(Viewer viewer, SQLQuery query, DBCPlan plan) {
		fillPlan(query, plan);
		showPlan(viewer, query, plan);
	}

	@Override
	protected void showPlan(Viewer viewer, SQLQuery query, DBCPlan plan) {
		PlanViewer planViewer = (PlanViewer) viewer;
		planViewer.showPlan(query, plan);
	}

}
