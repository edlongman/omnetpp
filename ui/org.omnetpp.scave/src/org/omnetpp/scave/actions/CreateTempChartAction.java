package org.omnetpp.scave.actions;

import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.omnetpp.common.image.ImageFactory;
import org.omnetpp.scave.editors.ScaveEditor;
import org.omnetpp.scave.editors.datatable.FilteredDataPanel;
import org.omnetpp.scave.model.Chart;
import org.omnetpp.scave.model.Dataset;
import org.omnetpp.scave.model.ResultType;
import org.omnetpp.scave.model.ScaveModelPackage;
import org.omnetpp.scave.model2.ScaveModelUtil;

/**
 * Creates a temporary dataset from the selection on the BrowseDataPage and
 * creates a temporary chart containing the items of the dataset.
 * It opens a chart page to display the chart.
 */
public class CreateTempChartAction extends AbstractScaveAction {
	static int counter = 0;
	
	public CreateTempChartAction() {
		setText("Plot");
		setToolTipText("Plot");
		setImageDescriptor(ImageFactory.getDescriptor(ImageFactory.TOOLBAR_IMAGE_PLOT));
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doRun(ScaveEditor editor, IStructuredSelection selection) {
		FilteredDataPanel activePanel = editor.getBrowseDataPage().getActivePanel();
		if (activePanel == null || activePanel.getTable().getSelectionCount() == 0)
			return;

		String datasetName = "dataset";
		Dataset dataset =
			ScaveModelUtil.createDataset(
					datasetName,
					activePanel.getTable().getSelectedItems(),
					null);
		String chartName = "temp" + ++counter; //FIXME generate proper name
		ResultType type = activePanel.getTable().getType();
		Chart chart = ScaveModelUtil.createChart(chartName, type);
		dataset.getItems().add(chart);

		editor.executeCommand(AddCommand.create(
				editor.getEditingDomain(),
				editor.getTempAnalysis().getDatasets(),
				ScaveModelPackage.eINSTANCE.getDatasets_Datasets(),
				dataset));
		editor.openChart(chart);
	}

	@Override
	protected boolean isApplicable(ScaveEditor editor, IStructuredSelection selection) {
		FilteredDataPanel activePanel = editor.getBrowseDataPage().getActivePanel();
		return editor.getActiveEditorPage()==editor.getBrowseDataPage() && activePanel != null && activePanel.getTable().getSelectionCount() > 0;

		//TODO: wherever there is a vector(s) or scalars selected, we want to be able to plot them
		//XXX for this to work, BrowseDataPage should update the selection when I switch between the Vectors/Scalars/Histograms pages
		//return selection instanceof IDListSelection && selection.size() > 0;
	}
}
