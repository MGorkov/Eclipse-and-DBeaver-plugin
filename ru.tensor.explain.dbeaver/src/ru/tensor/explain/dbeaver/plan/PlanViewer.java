package ru.tensor.explain.dbeaver.plan;

import java.net.URL;
import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.jkiss.dbeaver.ext.postgresql.model.plan.PostgrePlanNodeBase;
import org.jkiss.dbeaver.model.exec.plan.DBCPlan;
import org.jkiss.dbeaver.model.exec.plan.DBCPlanNode;
import org.jkiss.dbeaver.model.preferences.DBPPropertyDescriptor;
import org.jkiss.dbeaver.model.sql.SQLQuery;

import com.equo.chromium.swt.Browser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ru.tensor.explain.dbeaver.ExplainPostgreSQLPlugin;
import ru.tensor.explain.dbeaver.preferences.PreferenceConstants;

public class PlanViewer extends Viewer {
	ILog log = ExplainPostgreSQLPlugin.getDefault().getLog();
	IPreferenceStore store;
	private Browser fBrowser;
	private IWebBrowser eBrowser;
	Label label;
	private Pattern reDouble = Pattern.compile("^-?\\d+(\\.\\d+)?$");
	private Pattern reInteger = Pattern.compile("^-?\\d+$");

	public PlanViewer(IWorkbenchPart workbenchPart, Composite parent) {
		super();
		store = ExplainPostgreSQLPlugin.getDefault().getPreferenceStore();
		try {
			fBrowser = new Browser(parent, SWT.NONE);
		} catch (Throwable thr) {
			store.setValue(PreferenceConstants.P_EXTERNAL, true);
			label = new Label (parent, SWT.CENTER);
			label.setText("chromium browser is not supported");
			log.log(new Status(IStatus.ERROR,
					ExplainPostgreSQLPlugin.PLUGIN_ID,
					"createBrowser failed: " + thr.getMessage()
				)
			);
		}
	}

	@Override
	public Control getControl() {
		if (label != null) {
			return label;
		} else {
			return fBrowser;
		}
	}

	@Override
	public Object getInput() {
		return null;
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void refresh() {

	}

	@Override
	public void setInput(Object input) {

	}

	@Override
	public void setSelection(ISelection selection, boolean reveal) {

	}
	
	public void showPlan(SQLQuery query, DBCPlan plan) {
		JsonObject root = new JsonObject();
		for (DBCPlanNode node : plan.getPlanNodes(null)) {
			root.add("Plan", serializeNode(node));
		}

		try {
			boolean useExternalBrowser = store.getBoolean(PreferenceConstants.P_EXTERNAL);
			if (useExternalBrowser) {
				if (eBrowser == null) {
					IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
					try {
						eBrowser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EXTERNAL, "ru.tensor.expain", "Explain PostgreSQL", "Explain PostgreSQL");
					} catch (PartInitException ex) {
						log.log(new Status(IStatus.ERROR,
									ExplainPostgreSQLPlugin.PLUGIN_ID,
									"createBrowser failed: " + ex.getMessage()
								)
						);
					}
				}
			} else if (eBrowser != null) {
				eBrowser.close();
				eBrowser = null;
			}
			String url = ExplainPostgreSQLPlugin.getExplainAPI().plan_archive(root.toString(), query.getText()).join();
			if (eBrowser != null) {
				eBrowser.openURL(new URL(url));
			} else if (fBrowser != null) {
				fBrowser.setUrl(url);
			} else {
				log.log(new Status(IStatus.ERROR,
						ExplainPostgreSQLPlugin.PLUGIN_ID,
						"No browser for explain"
					)
				);
			}
		} catch (Exception ex) {
			log.log(new Status(IStatus.ERROR,
						ExplainPostgreSQLPlugin.PLUGIN_ID,
						"Explain failed! Error: " + ex.getMessage()
					)
			);
			MessageDialog.openError(null, "Explain PostgreSQL explainer", ex.getMessage());
		}

	}
	
	private JsonObject getNodeAttr(DBCPlanNode node) {
		if (node instanceof PostgrePlanNodeBase) {
			PostgrePlanNodeBase<?> pgNode = (PostgrePlanNodeBase<?>) node;
			JsonObject attr = new JsonObject();
			DBPPropertyDescriptor[] properties = pgNode.getProperties();
			attr.addProperty("Node Type", pgNode.getNodeType());
			for (DBPPropertyDescriptor p : properties) {
				String id = p.getId();
				Object value = pgNode.getPropertyValue(null, id);
				String jsonId = id.replace("I-O", "I/O").replace('-', ' ');
				if (reInteger.matcher(value.toString()).matches()) {
					attr.addProperty(jsonId, Integer.parseInt(value.toString()));
				} else if (reDouble.matcher(value.toString()).matches()) {
					attr.addProperty(jsonId, Double.parseDouble(value.toString()));
				} else if (value.equals("true")) {
					attr.addProperty(jsonId, true);
				} else if (value.equals("false")) {
					attr.addProperty(jsonId, false);
				} else {
					attr.addProperty(jsonId, value.toString());
				}
			}
			return attr;
		}
		return null;

	}
	
	private JsonElement serializeNode(DBCPlanNode node) {
		JsonObject nodeJson = getNodeAttr(node);
		Collection<? extends DBCPlanNode> nested = node.getNested();
		if (nested.size() > 0) {
			JsonArray nodes = new JsonArray();
			for (DBCPlanNode childNode: nested) {
				nodes.add(serializeNode(childNode));
			}
			nodeJson.add("Plans", nodes);
		}
		return nodeJson;
	}
	
}
