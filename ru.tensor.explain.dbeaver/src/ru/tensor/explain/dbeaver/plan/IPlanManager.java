package ru.tensor.explain.dbeaver.plan;

public interface IPlanManager {
	
	/**
	 * Adds listener
	 * 
	 * @param listener a listener
	 */
	public void addPlanListener(IPlanListener listener);
	
	/**
	 * Removes listener
	 * 
	 * @param listener a listener
	 */
	public void removePlanListener(IPlanListener listener);
	
	/**
	 * Fires all the listeners of the plan ready
	 * 
	 * @param plan the plan string
	 * @param query the query string
	 * @param mode explain view mode
	 */
	public void firePlan(String plan, String query, int mode);
	
	/**
	 * Create explain view
	 * 
	 * @param mode VIEW_ACTIVATE|VIEW_VISIBLE|VIEW_CREATE
	 */
	public void checkView(int mode);

}
