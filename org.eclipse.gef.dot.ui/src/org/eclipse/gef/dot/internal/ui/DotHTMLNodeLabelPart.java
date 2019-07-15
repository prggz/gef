package org.eclipse.gef.dot.internal.ui;

import javafx.scene.Group;

public class DotHTMLNodeLabelPart extends DotNodeLabelPart {

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

	protected void refreshHtmlLabelNode() {
		javafx.scene.Node fx = DotProperties
				.getHtmlLikeExternalLabel(getContent().getKey());
		if (fx != null && !getVisual().getChildren().contains(fx)) {
			getVisual().getChildren().clear();
			getVisual().getChildren().add(fx);
		}
	}
}
