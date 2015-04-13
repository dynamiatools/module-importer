/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.dynamia.modules.importacion.ui;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Button;
import org.zkoss.zul.Center;
import org.zkoss.zul.East;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.North;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.South;
import org.zkoss.zul.Window;

import tools.dynamia.actions.ActionEvent;
import tools.dynamia.actions.ActionEventBuilder;
import tools.dynamia.commons.Callback;
import tools.dynamia.integration.ProgressMonitor;
import tools.dynamia.modules.importacion.ImportAction;
import tools.dynamia.modules.importacion.ImportOperation;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.viewers.util.Viewers;
import tools.dynamia.zk.actions.ActionToolbar;
import tools.dynamia.zk.viewers.table.TableView;

/**
 *
 * @author mario_2
 */
public class Importer extends Window implements ActionEventBuilder {

	private ActionToolbar toolbar = new ActionToolbar(this);

	private Progressmeter progress = new Progressmeter();
	private Label progressLabel = new Label();

	private Borderlayout layout = new Borderlayout();
	private ImportAction currentAction;
	private ImportOperation currentOperation;

	private Button btnProcesar;
	private Button btnCancelar;

	private boolean operationRunning;

	public Importer() {
		buildLayout();

	}

	public void initTable(Class valueClass, List value) {

		layout.getCenter().getChildren().clear();

		TableView table = (TableView) Viewers.getView(valueClass, "table", value);
		table.setVflex(true);
		table.setSizedByContent(true);

		layout.getCenter().appendChild(table);
		progress.setValue(100);
	}

	@Override
	public ActionEvent buildActionEvent(Object source, Map<String, Object> params) {

		resetProgress();
		return new ActionEvent(null, this, params);
	}

	private void resetProgress() {
		progress.setVisible(false);
		progress.setValue(0);
		progressLabel.setValue("");
		Clients.clearBusy(layout.getCenter().getFirstChild());
	}

	public void updateProgress(ProgressMonitor monitor) {

		if (monitor.getCurrent() > 0) {
			try {

				int value = (int) (monitor.getCurrent() * 100 / monitor.getMax());
				if (!progress.isVisible()) {
					progress.setVisible(true);
				}
				progress.setValue(value);
				progressLabel.setValue(monitor.getMessage());
				
				Clients.showBusy(layout.getCenter().getFirstChild(), monitor.getMessage());
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
		Hlayout controls = new Hlayout();
		layout.getSouth().appendChild(controls);

		this.btnProcesar = new Button("Procesar datos importados");
		btnProcesar.setStyle("margin:4px");
		btnProcesar.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				processImportedData();

			}
		});

		this.btnCancelar = new Button("Cancelar");
		btnCancelar.setStyle("margin:4px");
		btnCancelar.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {
				if (currentOperation == null) {
					return;
				}

				UIMessages.showQuestion("Esta seguro que desea cancelar: " + currentOperation.getName() + "?", new Callback() {

					@Override
					public void doSomething() {
						cancel();

					}
				});

			}
		});

		progress.setHflex("1");
		controls.appendChild(btnProcesar);
		controls.appendChild(btnCancelar);
		controls.appendChild(progress);
		// controls.appendChild(progressLabel);

		setOperationStatus(false);
	}

	private void processImportedData() {
		if (currentAction != null) {

			if (currentOperation != null) {
				UIMessages.showMessage("Existe un proceso de importacion ejecuntandose en este momento", MessageType.WARNING);
				return;
			}

			UIMessages.showQuestion("Esta seguro que desea procesar los datos importados? Esta accion puede tardar varios minutos.",
					new Callback() {

						@Override
						public void doSomething() {
							resetProgress();
							currentAction.processImportedData(Importer.this);
						}
					});

		} else {
			UIMessages.showMessage("NO HAY DATOS QUE PROCESAR", MessageType.WARNING);
		}
	}

	public void cancel() {
		if (currentOperation != null) {
			currentOperation.cancelGracefully();
			currentOperation = null;

		}
	}

	public void addAction(ImportAction action) {
		toolbar.addAction(action);
	}

	public void setCurrentAction(ImportAction importAction) {
		this.currentAction = importAction;
	}

	public void setCurrentOperation(ImportOperation currentProcess) {
		this.currentOperation = currentProcess;
	}

	public ImportOperation getCurrentOperation() {
		return currentOperation;
	}

	public void setOperationStatus(boolean running) {
		this.operationRunning = running;
		checkRunning();
	}

	@Override
	public void onClose() {
		cancel();
	}

	private void checkRunning() {
		btnCancelar.setDisabled(!operationRunning);
		btnProcesar.setDisabled(operationRunning);
		setClosable(!operationRunning);

		for (Component child : toolbar.getChildren()) {
			if (child instanceof Button) {
				Button button = (Button) child;
				button.setDisabled(operationRunning);
			}
		}

		if (!isOperationRunning()) {
			resetProgress();
		}
	}

	public boolean isOperationRunning() {
		return operationRunning;
	}

}
