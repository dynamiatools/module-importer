
package tools.dynamia.modules.importacion;

import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.modules.importacion.ui.Importer;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;

/**
 *
 * @author Mario Serrano Leones
 */
public abstract class ImportAction extends AbstractAction {

	private ProgressMonitor monitor;
	private boolean procesable = true;

	@Override
	public void actionPerformed(ActionEvent evt) {
		Importer win = (Importer) evt.getSource();
		actionPerformed(win);
		if (isProcesable()) {
			win.setCurrentAction(this);
		}
	}

	public abstract void actionPerformed(Importer importer);

	public abstract void processImportedData(Importer importer);

	@Override
	public ActionRenderer getRenderer() {
		return new ToolbarbuttonActionRenderer(true);
	}

	public void setMonitor(ProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public ProgressMonitor getMonitor() {
		return monitor;
	}

	public boolean isProcesable() {
		return procesable;
	}

}
