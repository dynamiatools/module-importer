package com.dynamia.modules.importacion;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;

import com.dynamia.modules.importacion.ui.Importer;
import com.dynamia.tools.commons.DateTimeUtils;
import com.dynamia.tools.domain.ValidationError;
import com.dynamia.tools.integration.ProgressMonitor;
import com.dynamia.tools.integration.ProgressMonitorCallback;
import com.dynamia.tools.web.ui.MessageType;
import com.dynamia.tools.web.ui.UIMessages;
import com.dynamia.tools.web.util.LongOperation;

public abstract class ImportOperation extends LongOperation implements ProgressMonitorCallback {

	private ProgressMonitor monitor;
	private Importer importer;
	private String name;
	private long updateProgressRate = 3000;
	private long lastCheckTime;
	private long currentTime;
	private boolean cancelledGracefully;
	private long startTime = 0;

	public ImportOperation(String name, Importer importer) {
		super();
		this.name = name;
		this.importer = importer;
	}

	public ImportOperation(String name, Importer importer, long updateProgressRate) {
		super();
		this.name = name;
		this.importer = importer;
		this.updateProgressRate = updateProgressRate;
	}

	@Override
	public void progressChanged() {
		try {

			currentTime = System.currentTimeMillis();

			if (lastCheckTime == 0) {
				lastCheckTime = currentTime;
			}

			long elapsedTime = currentTime - startTime;
			if ((currentTime - lastCheckTime) >= updateProgressRate) {
				lastCheckTime = currentTime;
				
				double position = monitor.getCurrent();
				double total = monitor.getMax();
				
				if (startTime == 0)			{
					startTime = currentTime;
				}

				
				long estimatedRemaining = (long) (elapsedTime / position * (total-position) );
				
				
				
				activate();
				monitor.setMessage(monitor.getMessage() + " - Faltan <b>" + DurationFormatUtils.formatDuration(estimatedRemaining,"HH:mm:ss")+"</b> (h:m:s)");
				importer.updateProgress(monitor);
				deactivate();
			}
		} catch (Exception e) {
			// Ignore

		}
	}

	@Override
	protected void execute() throws InterruptedException {
		monitor = new ProgressMonitor(this);
		try {
			if (checkCurrentOperation()) {

				importer.setCurrentOperation(this);
				setOperationStatus(true);

				execute(monitor);

				importer.setCurrentOperation(null);
				setOperationStatus(false);
			} else {
				setOperationStatus(false);
				cancel();
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (ValidationError e) {
			try {
				activate();
				UIMessages.showMessage(e.getMessage(), MessageType.ERROR);
				importer.setOperationStatus(false);
				deactivate();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		} catch (Exception e) {
			throw new ImportOperationException(e);
		}
	}

	public void cancelGracefully() {
		cancelledGracefully = true;
		monitor.setMax(-1);
		monitor.setCurrent(-1);
	}

	@Override
	protected final void onFinish() {
		if (!cancelledGracefully) {
			onFinish(monitor);
		} else {
			onCancel();
		}
	}

	protected void onFinish(ProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	@Override
	protected final void onCancel() {
		onCancel(monitor);
	}

	protected void onCancel(ProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	private void setOperationStatus(boolean b) {
		try {
			activate();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		importer.setOperationStatus(b);
		deactivate();
	}

	public String getName() {
		return name;
	}

	private boolean checkCurrentOperation() {
		if (importer.getCurrentOperation() != null) {
			UIMessages.showMessage("No puede ejecutar este proceso, existe una operacion de importacion activa "
					+ importer.getCurrentOperation().getName(), null);
			return false;
		} else {
			return true;
		}
	}

	public abstract void execute(ProgressMonitor monitor) throws Exception;

}
