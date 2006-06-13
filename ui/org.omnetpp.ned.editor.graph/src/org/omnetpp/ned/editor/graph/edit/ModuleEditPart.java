package org.omnetpp.ned.editor.graph.edit;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;
import org.omnetpp.ned.editor.graph.edit.policies.NedComponentEditPolicy;
import org.omnetpp.ned.editor.graph.edit.policies.NedNodeEditPolicy;
import org.omnetpp.ned.editor.graph.figures.GateAnchor;
import org.omnetpp.ned.editor.graph.figures.ModuleFigure;
import org.omnetpp.ned2.model.ConnectionNodeEx;
import org.omnetpp.ned2.model.INamedGraphNode;
import org.omnetpp.ned2.model.NEDElement;

/**
 * Base abstract controller for NedModel and NedFigures. Provides support for 
 * connection handling and common display attributes.
 */
abstract public class ModuleEditPart extends ContainerEditPart implements NodeEditPart {

	@Override
	protected void createEditPolicies() {
		super.createEditPolicies();
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new NedComponentEditPolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NedNodeEditPolicy());
	}


	/**
	 * Returns a list of connections for which this is the srcModule.
	 * 
	 * @return List of connections.
	 */
	@Override
	protected List getModelSourceConnections() {
		return ((INamedGraphNode)getNEDModel()).getSrcConnections();
	}

	/**
	 * Returns a list of connections for which this is the destModule.
	 * 
	 * @return List of connections.
	 */
	@Override
	protected List getModelTargetConnections() {
		return ((INamedGraphNode)getNEDModel()).getDestConnections();
	}

	/**
	 * Returns the Figure of this, as a NedFigure.
	 * 
	 * @return Figure as a NedFigure.
	 */
	protected ModuleFigure getNedFigure() {
		return (ModuleFigure) getFigure();
	}
	
	/**
	 * @return The names of the input gates
	 */
	abstract String[] getInputGateNames();
	
	/**
	 * @return The names of the output gates
	 */
	abstract String[] getOutputGateNames();


	/**
	 * Compute the source connection anchor to be assigned based on the current mouse 
	 * location and available gates. 
	 * @param p current mouse coordinates
	 * @return The selected connection anchor
	 */
	public ConnectionAnchor getSourceConnectionAnchorAt(Point p) {
		// TODO select the appropriate gate name automatically
		// or return NULL if no output gate is available
		return new GateAnchor(getFigure(), getOutputGateNames()[0]);
	}

	/**
	 * Compute the target connection anchor to be assigned based on the current mouse 
	 * location and available gates. 
	 * @param p
	 * @return
	 */
	public ConnectionAnchor getTargetConnectionAnchorAt(Point p) {
		// TODO select the appropriate gate name automatically
		// or return NULL if no input gate is available
		return new GateAnchor(getFigure(), getInputGateNames()[0]);
	}

	/**
	 * Returns a conn anchor registered for the given gate
	 * @param gate
	 * @return
	 */
	public GateAnchor getConnectionAnchor(String gate) {
//		if (gate == null)
//			return null;
//
//		GateAnchor ga = targetConnectionAnchors.get(gate);
//		if (ga != null)	return ga;
//
//		ga = sourceConnectionAnchors.get(gate);
//		return ga;
		return new GateAnchor(getFigure(), gate);
	}

	/**
	 * Returns the connection anchor for the given ConnectionEditPart's srcModule.
	 * 
	 * @return ConnectionAnchor.
	 */
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connEditPart) {
		ConnectionNodeEx conn = (ConnectionNodeEx) connEditPart.getModel();
		return getConnectionAnchor(conn.getSrcGateWithIndex());
	}

	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getSourceConnectionAnchorAt(pt);
	}

	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connEditPart) {
		ConnectionNodeEx conn = (ConnectionNodeEx) connEditPart.getModel();
		return getConnectionAnchor(conn.getDestGateWithIndex());
	}

	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		Point pt = new Point(((DropRequest) request).getLocation());
		return getTargetConnectionAnchorAt(pt);
	}

	/* (non-Javadoc)
	 * @see org.omnetpp.ned.editor.graph.edit.ContainerEditPart#attributeChanged(org.omnetpp.ned2.model.NEDElement, java.lang.String)
	 */
	@Override
	public void attributeChanged(NEDElement node, String attr) {
		super.attributeChanged(node, attr);

		// SubmoduleNodeEx and CompoundModuleNodeEx fire ATT_SRC(DEST)_CONNECTION 
		// attribute change if a connection gets added/removed
		if (INamedGraphNode.ATT_SRC_CONNECTION.equals(attr))
			refreshSourceConnections();
		else if (INamedGraphNode.ATT_DEST_CONNECTION.equals(attr))
			refreshTargetConnections();
	}

}
