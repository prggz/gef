package org.eclipse.gef.dot.internal.ui;

import org.eclipse.gef.graph.Edge;
import org.eclipse.gef.zest.fx.ZestProperties;

import javafx.scene.Group;
import javafx.scene.Node;

public class DotHTMLEdgeLabelPart extends DotEdgeLabelPart {

	@Override
	protected Group doCreateVisual() {
		createText(); // to avoid NPE
		return new Group();
	}

	@Override
	protected void doRefreshVisual(Group visual) {
		super.doRefreshVisual(visual);
		refreshHtmlLabelNode();
	}

	// protected void refreshHtmlLabelText() {
	// String label = getText().getText();
	// if (label == null || label.isEmpty()) {
	// label = ""; //$NON-NLS-1$
	// }
	// if (htmlNode != null && !label.equals(htmlNode.getLabel())) {
	// htmlNode.setLabel(label);
	// }
	// }

	protected void refreshHtmlLabelNode() {
		Node fx = getHtmlLabelNode();
		if (fx != null && !getVisual().getChildren().contains(fx)) {
			getVisual().getChildren().clear();
			getVisual().getChildren().add(fx);
		}
	}

	protected Node getHtmlLabelNode() {
		Edge edge = getContent().getKey();
		String attributeName = getContent().getValue();
		if (ZestProperties.LABEL__NE.equals(attributeName)) {
			return DotProperties.getHtmlLikeLabel(edge);
		} else if (ZestProperties.EXTERNAL_LABEL__NE.equals(attributeName)) {
			return DotProperties.getHtmlLikeExternalLabel(edge);
		} else if (ZestProperties.SOURCE_LABEL__E.equals(attributeName)) {
			return DotProperties.getHtmlLikeSourceLabel(edge);
		} else if (ZestProperties.TARGET_LABEL__E.equals(attributeName)) {
			return DotProperties.getHtmlLikeTargetLabel(edge);
		}
		return null;
	}
}
