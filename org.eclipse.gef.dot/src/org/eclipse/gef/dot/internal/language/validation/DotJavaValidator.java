/*******************************************************************************
 * Copyright (c) 2009, 2017 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabian Steeg    - intial Xtext generation (see bug #277380)
 *     Alexander Nyßen - initial implementation
 *     Tamas Miklossy  - Add support for arrowType edge decorations (bug #477980)
 *                     - Add support for polygon-based node shapes (bug #441352)
 *                     - Add support for all dot attributes (bug #461506)
 *     Zoey Gerrit Prigge - Add support for record label attributes (bug #454629)
 *
 *******************************************************************************/

package org.eclipse.gef.dot.internal.language.validation;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.common.reflect.ReflectionUtils;
import org.eclipse.gef.dot.internal.DotAttributes;
import org.eclipse.gef.dot.internal.DotAttributes.Context;
import org.eclipse.gef.dot.internal.language.DotAstHelper;
import org.eclipse.gef.dot.internal.language.DotRecordLabelStandaloneSetup;
import org.eclipse.gef.dot.internal.language.dot.AttrList;
import org.eclipse.gef.dot.internal.language.dot.AttrStmt;
import org.eclipse.gef.dot.internal.language.dot.Attribute;
import org.eclipse.gef.dot.internal.language.dot.DotGraph;
import org.eclipse.gef.dot.internal.language.dot.DotPackage;
import org.eclipse.gef.dot.internal.language.dot.EdgeOp;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsNode;
import org.eclipse.gef.dot.internal.language.dot.EdgeRhsSubgraph;
import org.eclipse.gef.dot.internal.language.dot.GraphType;
import org.eclipse.gef.dot.internal.language.dot.NodeStmt;
import org.eclipse.gef.dot.internal.language.shape.PolygonBasedNodeShape;
import org.eclipse.gef.dot.internal.language.shape.RecordBasedNodeShape;
import org.eclipse.gef.dot.internal.language.style.NodeStyle;
import org.eclipse.gef.dot.internal.language.terminals.ID;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.validation.AbstractInjectableValidator;
import org.eclipse.xtext.validation.Check;
import org.eclipse.xtext.validation.RangeBasedDiagnostic;

import com.google.inject.Injector;

/**
 * Provides DOT-specific validation rules.
 * 
 * @author anyssen
 *
 */
public class DotJavaValidator extends AbstractDotJavaValidator {

	/**
	 * Checks that within an {@link Attribute} only valid attribute values are
	 * used (dependent on context, in which the attribute is specified).
	 * 
	 * @param attribute
	 *            The {@link Attribute} to validate.
	 */
	@Check
	public void checkValidAttributeValue(final Attribute attribute) {
		String attributeName = attribute.getName().toValue();
		ID attributeValue = attribute.getValue();

		// give the DotColorValidator the necessary 'global' information
		DotColorJavaValidator.considerDefaultColorScheme = true;
		DotColorJavaValidator.globalColorScheme = DotAstHelper
				.getColorSchemeAttributeValue(attribute);

		List<Diagnostic> diagnostics = DotAttributes.validateAttributeRawValue(
				DotAttributes.getContext(attribute), attributeName,
				attributeValue);

		// reset the state of the DotColorValidator
		DotColorJavaValidator.globalColorScheme = null;
		DotColorJavaValidator.considerDefaultColorScheme = false;

		List<INode> nodes = NodeModelUtils.findNodesForFeature(attribute,
				DotPackage.Literals.ATTRIBUTE__VALUE);
		if (nodes.size() != 1) {
			System.err.println(
					"Exact 1 node is expected for the attribute value: "
							+ attributeValue + ", but got " + nodes.size());
			return;
		}

		INode node = nodes.get(0);
		int attributeValueStartOffset = node.getOffset();
		if (attributeValue.getType() == ID.Type.HTML_STRING
				|| attributeValue.getType() == ID.Type.QUOTED_STRING) {
			// +1 is needed because of the < symbol (indicating the
			// beginning of a html-like label) or " symbol (indicating the
			// beginning of a quoted string)
			attributeValueStartOffset++;
		}

		for (Diagnostic d : diagnostics) {
			if (d instanceof RangeBasedDiagnostic) {
				RangeBasedDiagnostic rangeBasedDiagnostic = (RangeBasedDiagnostic) d;
				String message = rangeBasedDiagnostic.getMessage();
				int length = rangeBasedDiagnostic.getLength();
				String code = rangeBasedDiagnostic.getIssueCode();
				String[] issueData = rangeBasedDiagnostic.getIssueData();
				int offset = rangeBasedDiagnostic.getOffset()
						+ attributeValueStartOffset;
				switch (d.getSeverity()) {
				case Diagnostic.ERROR:
					getMessageAcceptor().acceptError(message, attribute, offset,
							length, code, issueData);
					break;

				case Diagnostic.WARNING:
					getMessageAcceptor().acceptWarning(message, attribute,
							offset, length, code, issueData);
					break;

				case Diagnostic.INFO:
					getMessageAcceptor().acceptError(message, attribute, offset,
							length, code, issueData);
					break;

				}
			} else {
				switch (d.getSeverity()) {
				case Diagnostic.ERROR:
					getMessageAcceptor().acceptError(d.getMessage(), attribute,
							DotPackage.Literals.ATTRIBUTE__VALUE,
							INSIGNIFICANT_INDEX, attributeName,
							attributeValue.toValue());
					break;

				case Diagnostic.WARNING:
					getMessageAcceptor().acceptWarning(d.getMessage(),
							attribute, DotPackage.Literals.ATTRIBUTE__VALUE,
							INSIGNIFICANT_INDEX, attributeName,
							attributeValue.toValue());
					break;
				case Diagnostic.INFO:
					getMessageAcceptor().acceptInfo(d.getMessage(), attribute,
							DotPackage.Literals.ATTRIBUTE__VALUE,
							INSIGNIFICANT_INDEX, attributeName,
							attributeValue.toValue());
					break;

				}
			}
		}
	}

	/**
	 * Ensures that the 'striped' node style is used only for
	 * rectangularly-shaped nodes ('box', 'rect', 'rectangle' and 'square').
	 * 
	 * @param attribute
	 *            The node style attribute to validate.
	 */
	@Check
	public void checkValidCombinationOfNodeShapeAndStyle(Attribute attribute) {
		if (DotAttributes.getContext(attribute) == Context.NODE
				&& attribute.getName().toValue()
						.equals(DotAttributes.STYLE__GCNE)
				&& attribute.getValue().toValue()
						.equals(NodeStyle.STRIPED.toString())) {
			EList<AttrList> attributeList = null;
			NodeStmt node = EcoreUtil2.getContainerOfType(attribute,
					NodeStmt.class);
			if (node != null) {
				attributeList = node.getAttrLists();
			} else {
				AttrStmt attrStmt = EcoreUtil2.getContainerOfType(attribute,
						AttrStmt.class);
				if (attrStmt != null) {
					attributeList = attrStmt.getAttrLists();
				}
			}

			if (attributeList != null) {
				ID shapeValue = DotAstHelper.getAttributeValue(attributeList,
						DotAttributes.SHAPE__N);
				// if the shape value is not explicitly set, use the default
				// shape value for evaluation
				if (shapeValue == null) {
					shapeValue = ID.fromString(
							PolygonBasedNodeShape.ELLIPSE.toString());
				}
				switch (PolygonBasedNodeShape.get(shapeValue.toValue())) {
				case BOX:
				case RECT:
				case RECTANGLE:
				case SQUARE:
					break;
				default:
					error("The style 'striped' is only supported with clusters and rectangularly-shaped nodes, such as 'box', 'rect', 'rectangle', 'square'.",
							DotPackage.eINSTANCE.getAttribute_Value());
				}
			}
		}
	}

	/**
	 * Ensures that within {@link EdgeRhsNode}, '-&gt;' is used in directed
	 * graphs, while '--' is used in undirected graphs.
	 * 
	 * @param edgeRhsNode
	 *            The EdgeRhsNode to validate.
	 */
	@Check
	public void checkEdgeOpCorrespondsToGraphType(EdgeRhsNode edgeRhsNode) {
		checkEdgeOpCorrespondsToGraphType(edgeRhsNode.getOp(), EcoreUtil2
				.getContainerOfType(edgeRhsNode, DotGraph.class).getType());
	}

	/**
	 * Ensures that within {@link EdgeRhsSubgraph} '-&gt;' is used in directed
	 * graphs, while '--' is used in undirected graphs.
	 * 
	 * @param edgeRhsSubgraph
	 *            The EdgeRhsSubgraph to validate.
	 */
	@Check
	public void checkEdgeOpCorrespondsToGraphType(
			EdgeRhsSubgraph edgeRhsSubgraph) {
		checkEdgeOpCorrespondsToGraphType(edgeRhsSubgraph.getOp(), EcoreUtil2
				.getContainerOfType(edgeRhsSubgraph, DotGraph.class).getType());
	}

	private void checkEdgeOpCorrespondsToGraphType(EdgeOp edgeOp,
			GraphType graphType) {
		boolean edgeDirected = edgeOp.equals(EdgeOp.DIRECTED);
		boolean graphDirected = graphType.equals(GraphType.DIGRAPH);
		if (graphDirected && !edgeDirected) {
			error("EdgeOp '--' may only be used in undirected graphs.",
					DotPackage.eINSTANCE.getEdgeRhs_Op());

		} else if (!graphDirected && edgeDirected) {
			error("EdgeOp '->' may only be used in directed graphs.",
					DotPackage.eINSTANCE.getEdgeRhs_Op());
		}
	}

	/**
	 * Use to run the recordLabel subgrammar validation on relevant labels
	 * (label attributes on Nodes where a recordBased label shape attribute
	 * exists).
	 * 
	 * @param attribute
	 *            The attribute to validate.
	 */
	@Check
	public void checkRecordBasedNodeShapeValue(Attribute attribute) {
		if (DotAttributes.getContext(attribute).equals(Context.NODE)
				&& attribute.getName().toValue()
						.equals(DotAttributes.LABEL__GCNE)) {
			String shapeValue = DotAstHelper.getDependedOnAttributeValue(
					attribute, DotAttributes.SHAPE__N);
			if (RecordBasedNodeShape.get(shapeValue) != null) {
				doRecordLabelValidation(attribute);
			}
		}
	}

	private void doRecordLabelValidation(Attribute attribute) {
		Injector recordLabelInjector = new DotRecordLabelStandaloneSetup()
				.createInjectorAndDoEMFRegistration();
		DotRecordLabelJavaValidator validator = recordLabelInjector
				.getInstance(DotRecordLabelJavaValidator.class);
		IParser parser = recordLabelInjector.getInstance(IParser.class);

		ConvertingValidationMessageAcceptor messageAcceptor = new ConvertingValidationMessageAcceptor(
				attribute, DotPackage.Literals.ATTRIBUTE__VALUE,
				attribute.getName().toString(), getMessageAcceptor(),
				"\"".length());

		validator.setMessageAcceptor(messageAcceptor);

		IParseResult result = parser
				.parse(new StringReader(attribute.getValue().toValue()));

		for (INode error : result.getSyntaxErrors()) {
			messageAcceptor.acceptSyntaxError(error);
		}

		Map<Object, Object> validationContext = new HashMap<Object, Object>();
		validationContext.put(AbstractInjectableValidator.CURRENT_LANGUAGE_NAME,
				ReflectionUtils.getPrivateFieldValue(validator,
						"languageName"));

		Iterator<EObject> iterator = result.getRootASTElement().eAllContents();
		while (iterator.hasNext()) {
			validator.validate(iterator.next(), null/* diagnostic chain */,
					validationContext);
		}

		validator.validate(result.getRootASTElement(), null, validationContext);
	}

}
