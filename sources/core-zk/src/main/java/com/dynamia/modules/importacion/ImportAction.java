/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dynamia.modules.importacion;

import com.dynamia.modules.importacion.ui.ImporterWindow;
import com.dynamia.tools.web.actions.AbstractAction;
import com.dynamia.tools.web.actions.ActionEvent;
import com.dynamia.tools.web.actions.ActionRenderer;
import com.dynamia.tools.web.crud.actions.renderers.ToolbarbuttonActionRenderer;

/**
 *
 * @author mario_2
 */
public abstract class ImportAction<DATA> extends AbstractAction {

    @Override
    public void actionPerformed(ActionEvent evt) {
        ImporterWindow win = (ImporterWindow) evt.getSource();
        actionPerformed(evt, win);
        win.setCurrentAction(this);

    }

    public abstract void actionPerformed(ActionEvent evt, ImporterWindow win);

    public abstract void processImportedData(DATA data, ImporterWindow win);

    @Override
    public ActionRenderer getRenderer() {
        return new ToolbarbuttonActionRenderer(true);
    }

}
