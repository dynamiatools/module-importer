/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dynamia.modules.importacion;

import com.dynamia.modules.importacion.ui.Importer;
import com.dynamia.tools.web.actions.AbstractAction;
import com.dynamia.tools.web.actions.ActionEvent;
import com.dynamia.tools.web.actions.ActionRenderer;
import com.dynamia.tools.web.crud.actions.renderers.ToolbarbuttonActionRenderer;

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
