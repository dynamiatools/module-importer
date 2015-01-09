/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dynamia.modules.importacion.ui;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.East;
import org.zkoss.zul.North;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.South;
import org.zkoss.zul.Window;

import com.dynamia.modules.importacion.ImportAction;
import com.dynamia.tools.integration.ProgressMonitor;
import com.dynamia.tools.integration.ProgressMonitorCallback;
import com.dynamia.tools.viewers.util.Viewers;
import com.dynamia.tools.viewers.zk.table.TableView;
import com.dynamia.tools.web.actions.ActionEvent;
import com.dynamia.tools.web.actions.ActionEventBuilder;
import com.dynamia.tools.web.ui.ActionToolbar;
import com.dynamia.tools.web.ui.MessageType;
import com.dynamia.tools.web.ui.UIMessages;
import com.dynamia.tools.web.util.Callback;

/**
 *
 * @author mario_2
 */
public class ImporterWindow extends Window implements ActionEventBuilder, ProgressMonitorCallback {

    private ActionToolbar toolbar = new ActionToolbar(this);

    private Progressmeter progress = new Progressmeter();
    private ProgressMonitor currentMonitor;
    private Borderlayout layout = new Borderlayout();
    private ImportAction currentAction;
    private List value;

    public ImporterWindow() {
        buildLayout();

    }

    public void setValue(Class valueClass, List value) {
        this.value = value;
        layout.getCenter().getChildren().clear();

        TableView table = (TableView) Viewers.getView(valueClass, "table", value);
        table.setSizedByContent(true);

        layout.getCenter().appendChild(table);
        progress.setValue(100);
    }

    @Override
    public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {

        currentMonitor = new ProgressMonitor(this);

        progress.setValue(0);
        return new ActionEvent(currentMonitor, this, params);
    }

    @Override
    public void progressChanged() {
        System.out.println(currentMonitor.getMessage());
    }

    private void updateProgress() {

        if (currentMonitor.getCurrent() > 0) {
            try {

                int value = (int) (currentMonitor.getCurrent() * 100 / currentMonitor.getMax());
                progress.setValue(value);

            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private void buildLayout() {
        setHflex("1");

        setVflex("1");
        appendChild(layout);
        layout.setHflex("1");
        layout.setVflex("1");

        layout.appendChild(new Center());
        layout.appendChild(new North());
        layout.appendChild(new South());
        layout.appendChild(new East());

        layout.getNorth().appendChild(toolbar);

        Button btnProcesar = new Button("Procesar datos importados");
        btnProcesar.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

            @Override
            public void onEvent(Event event) throws Exception {
                processImportedData();

            }
        });
        btnProcesar.setStyle("margin:4px");
        layout.getSouth().appendChild(btnProcesar);
        progress.setHflex("1");

    }

    private void processImportedData() {
        if (currentAction != null && value != null) {
            UIMessages.showQuestion("Esta seguro que desea procesar los datos importados? Esta accion puede tardar varios minutos.",
                    new Callback() {

                        @Override
                        public void doSomething() {
                            currentMonitor = new ProgressMonitor(ImporterWindow.this);
                            currentAction.processImportedData(value, ImporterWindow.this);
                        }
                    });

        } else {
            UIMessages.showMessage("NO HAY DATOS QUE PROCESAR", MessageType.WARNING);
        }
    }

    public void addAction(ImportAction action) {
        toolbar.addAction(action);
    }

    public ProgressMonitor getCurrentMonitor() {
        return currentMonitor;
    }

    public void setCurrentAction(ImportAction importAction) {
        this.currentAction = importAction;

    }

}
