package ru.tensor.explain.eclipse;

import org.eclipse.datatools.sqltools.sqleditor.ISQLEditorActionConstants;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditor;
import org.eclipse.datatools.sqltools.sqleditor.SQLEditorActionContributorExtension;
import org.eclipse.datatools.sqltools.sqleditor.plan.ExplainSQLActionDelegate;
import org.eclipse.jface.action.IMenuManager;

public class PostgresSQLEditorActionContributorExtension extends SQLEditorActionContributorExtension {
	
	private SQLEditor sqlEditor;

	@Override
	public void setActiveEditor(SQLEditor targetEditor) {
		sqlEditor = targetEditor;
	}

	@Override
	public void contributeToContextMenu(IMenuManager mm) {
		mm.prependToGroup(ISQLEditorActionConstants.GROUP_SQLEDITOR_EXECUTE, new ExplainSQLActionDelegate(this.sqlEditor));
		super.contributeToContextMenu(mm);
	}

}
