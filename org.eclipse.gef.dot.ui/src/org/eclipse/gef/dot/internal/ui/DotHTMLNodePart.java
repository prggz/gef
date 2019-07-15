package org.eclipse.gef.dot.internal.ui;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;

public class DotHTMLNodePart extends DotNodePart {

	private Pane htmlLabelParentVisual;

	@Override
	protected Group doCreateVisual() {
		Group visual = super.doCreateVisual();
		Parent labelParent = findFxPaneWithText(visual, getLabelText());
		if (labelParent instanceof Pane) {
			htmlLabelParentVisual = ((Pane) labelParent);
			htmlLabelParentVisual.getChildren().remove(getLabelText());
		} else {
			new RuntimeException("HTML label could not be visualized"); //$NON-NLS-1$
		}
		return visual;
	}

	private Parent findFxPaneWithText(Parent group, javafx.scene.Node text) {
		for (javafx.scene.Node node : group.getChildrenUnmodifiable()) {
			if (node == text) {
				return group;
			} else if (node instanceof Parent) {
				Parent possibleParent = findFxPaneWithText((Parent) node, text);
				if (possibleParent != null) {
					return possibleParent;
				}
			}
		}
		return null;
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// protected void doRefreshVisual(Group visual) {
	// super.doRefreshVisual(visual);
	// Node node = getContent();
	// htmlNode.setUtils(
	// (DotColorUtil) node.attributesProperty()
	// .get(DotProperties.COLORUTIL__NE),
	// (DotFontUtil) node.attributesProperty()
	// .get(DotProperties.FONTUTIL__NE));
	// htmlNode.setDefaults((Map<String, String>) node.attributesProperty()
	// .get(DotProperties.HTML_LIKE_DEFAULTS__NE));
	// htmlNode.refreshFxElement();
	// }

	@Override
	protected void doRefreshVisual(Group visual) {
		super.doRefreshVisual(visual);
		refreshHtmlLabelNode();
	}

	protected void refreshHtmlLabelNode() {
		javafx.scene.Node fx = DotProperties.getHtmlLikeLabel(getContent());
		if (fx != null && !htmlLabelParentVisual.getChildren().contains(fx)) {
			htmlLabelParentVisual.getChildren().clear();
			htmlLabelParentVisual.getChildren().add(fx);
		}
	}
}
