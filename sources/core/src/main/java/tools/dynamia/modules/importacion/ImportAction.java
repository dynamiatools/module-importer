/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.dynamia.modules.importacion;

import tools.dynamia.actions.AbstractAction;
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionRenderer;
import tools.dynamia.modules.importacion.ui.Importer;
import tools.dynamia.zk.actions.ToolbarbuttonActionRenderer;

/**
 *
 * @author mario_2
 */
public abstract class ImportAction extends AbstractAction {

	@Override
	public void actionPerformed(ActionEvent evt) {
		Importer win = (Importer) evt.getSource();
		actionPerformed(win);
		win.setCurrentAction(this);
	}

	public abstract void actionPerformed(Importer importer);

	public abstract void processImportedData(Importer importer);

	@Override
	public ActionRenderer getRenderer() {
		return new ToolbarbuttonActionRenderer(true);
	}

}
