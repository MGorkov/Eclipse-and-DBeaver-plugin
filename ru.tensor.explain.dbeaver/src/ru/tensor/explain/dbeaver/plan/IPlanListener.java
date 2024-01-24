package ru.tensor.explain.dbeaver.plan;

public interface IPlanListener {
	
	/**
	 * Notified when a new plan is created
	 * 
	 * @param plan the plan string
	 * @param query the query string
	 */
	public void planCreated(String plan, String query);

}
