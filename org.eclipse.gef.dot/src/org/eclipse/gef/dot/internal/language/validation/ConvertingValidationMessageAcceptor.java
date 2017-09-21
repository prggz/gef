/**
 * 
 */
package org.eclipse.gef.dot.internal.language.validation;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.validation.ValidationMessageAcceptor;

/**
 * @author zgerritprigge
 *
 */
public class ConvertingValidationMessageAcceptor
		implements ValidationMessageAcceptor {

	private final EObject hostingEObject;
	private final ValidationMessageAcceptor hostMessageAcceptor;
	private final String userReadableIdentifier;
	private int initialOffset;

	/**
	 * 
	 * 
	 * 
	 * @param hostingEObject
	 * @param hostingFeature
	 * @param userReadableIdentifier
	 * @param hostMessageAcceptor
	 * @param internalOffset
	 */
	public ConvertingValidationMessageAcceptor(EObject hostingEObject,
			EStructuralFeature hostingFeature, String userReadableIdentifier,
			ValidationMessageAcceptor hostMessageAcceptor, int internalOffset) {

		this.hostingEObject = hostingEObject;
		this.hostMessageAcceptor = hostMessageAcceptor;
		this.userReadableIdentifier = userReadableIdentifier;

		this.initialOffset = getFirstNodeForEObject(hostingEObject,
				hostingFeature).getOffset();
		this.initialOffset = calculateOffset(internalOffset);
	}

	/**
	 * @param error
	 */
	public void acceptSyntaxError(INode error) {
		SyntaxErrorMessage errorMessage = error.getSyntaxErrorMessage();
		hostMessageAcceptor.acceptError(
				buildMessage("Syntax", errorMessage.getMessage()),
				hostingEObject, calculateOffset(error.getOffset()),
				error.getLength(), errorMessage.getIssueCode(),
				errorMessage.getIssueData());
	}

	@Override
	public void acceptError(String message, EObject object,
			EStructuralFeature feature, int index, String code,
			String... issueData) {
		INode node = getFirstNodeForEObject(object, feature);
		hostMessageAcceptor.acceptError(buildMessage("Semantic", message),
				hostingEObject, calculateOffset(node.getOffset()),
				node.getLength(), code, issueData);
	}

	@Override
	public void acceptError(String message, EObject object, int offset,
			int length, String code, String... issueData) {
		hostMessageAcceptor.acceptError(buildMessage("Semantic", message),
				hostingEObject, calculateOffset(offset), length, code,
				issueData);
	}

	@Override
	public void acceptInfo(String message, EObject object,
			EStructuralFeature feature, int index, String code,
			String... issueData) {
		INode node = getFirstNodeForEObject(object, feature);
		hostMessageAcceptor.acceptInfo(buildMessage("Semantic", message),
				hostingEObject, calculateOffset(node.getOffset()),
				node.getLength(), code, issueData);
	}

	@Override
	public void acceptInfo(String message, EObject object, int offset,
			int length, String code, String... issueData) {
		hostMessageAcceptor.acceptInfo(buildMessage("Semantic", message),
				hostingEObject, calculateOffset(offset), length, code,
				issueData);
	}

	@Override
	public void acceptWarning(String message, EObject object,
			EStructuralFeature feature, int index, String code,
			String... issueData) {
		INode node = getFirstNodeForEObject(object, feature);
		hostMessageAcceptor.acceptWarning(buildMessage("Semantic", message),
				hostingEObject, calculateOffset(node.getOffset()),
				node.getLength(), code, issueData);
	}

	@Override
	public void acceptWarning(String message, EObject object, int offset,
			int length, String code, String... issueData) {
		hostMessageAcceptor.acceptWarning(buildMessage("Semantic", message),
				hostingEObject, calculateOffset(offset), length, code,
				issueData);
	}

	private int calculateOffset(int offset) {
		return offset >= 0 && initialOffset >= 0 ? offset + initialOffset
				: offset;
	}

	private INode getFirstNodeForEObject(EObject eObject,
			EStructuralFeature eStructuralFeature) {
		List<INode> nodes = NodeModelUtils.findNodesForFeature(eObject,
				eStructuralFeature);
		if (nodes.size() != 1) {
			System.err.println("Exactly 1 node is expected for the eObject "
					+ eObject + ", but got " + nodes.size());
			return null;
		}
		INode node = nodes.get(0);
		return node;
	}

	private String buildMessage(String errorType, String errorMessage) {
		StringBuilder message = new StringBuilder();
		message.append(errorType).append(" error on ")
				.append(hostingEObject.eClass().getName()).append(" ")
				.append(userReadableIdentifier).append(": ");
		message.append(errorMessage);
		String userMessage = message.toString();
		return userMessage;
	}

}
