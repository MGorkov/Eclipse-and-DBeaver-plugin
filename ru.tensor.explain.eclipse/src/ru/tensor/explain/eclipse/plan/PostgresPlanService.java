package ru.tensor.explain.eclipse.plan;

import org.eclipse.datatools.sqltools.plan.PlanRequest;
import org.eclipse.datatools.sqltools.plan.PlanService;
import org.eclipse.datatools.sqltools.plan.PlanSupportRunnable;

public class PostgresPlanService extends PlanService {

	@Override
	public PlanSupportRunnable createPlanSupportRunnable(PlanRequest request, String profileName, String dbName) {
		return new PostgresPlanSupportRunnable(request, profileName, dbName);
	}

}
