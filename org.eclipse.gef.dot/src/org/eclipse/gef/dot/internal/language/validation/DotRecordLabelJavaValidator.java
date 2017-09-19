/*******************************************************************************
 * Copyright (c) 2017 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zoey Gerrit Prigge  - initial API and implementation (bug #454629)
 *    
 *******************************************************************************/
package org.eclipse.gef.dot.internal.language.validation;

import static com.google.common.collect.Iterables.concat;
import static org.eclipse.xtext.EcoreUtil2.getAllContentsOfType;
import static org.eclipse.xtext.xbase.lib.IterableExtensions.filter;

import org.eclipse.gef.dot.internal.language.recordlabel.Port;
import org.eclipse.gef.dot.internal.language.recordlabel.RecordLabel;
import org.eclipse.gef.dot.internal.language.recordlabel.RecordlabelPackage;
import org.eclipse.gef.dot.internal.language.recordlabel.RotationWrapper;
import org.eclipse.xtext.validation.Check;

import com.google.common.collect.HashMultimap;

/**
 * This class contains custom validation rules.
 *
 * See
 * https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
public class DotRecordLabelJavaValidator extends
		org.eclipse.gef.dot.internal.language.validation.AbstractDotRecordLabelJavaValidator {

	/**
	 * Snippet including the subgrammar issue code prefix
	 */
	public final static String ISSUE_CODE_PREFIX = "org.eclipse.gef.dot.internal.language.dotRecordLabel.";

	/**
	 * Issue code for a duplicate port name error
	 */
	public final static String PORT_NAME_DUPLICATE = (ISSUE_CODE_PREFIX
			+ "PortNameDuplicate");

	/**
	 * Issue code for a unset port name warning
	 */
	public final static String PORT_NAME_NOT_SET = (ISSUE_CODE_PREFIX
			+ "PortNameNotSet");

	/**
	 * Checks that if a port is specified, it has a name which allows
	 * referencing.
	 * 
	 * @param port
	 *            Port to be checked.
	 */
	@Check
	public void checkPortNameIsNotNull(final Port port) {
		String name = port.getName();
		if (name == null) {
			warning("Port unnamed: port cannot be referenced",
					RecordlabelPackage.Literals.MAY_HAVE_NAME__NAME,
					PORT_NAME_NOT_SET);
		}
	}

	/**
	 * Checks that if a record based label has multiple ports none have the same
	 * name
	 * 
	 * @param label
	 *            RecordLabel to be checked.
	 */
	@Check
	public void checkMultiplePortsNotSameName(final RecordLabel label) {
		/*
		 * We shall only check the top layer label
		 */
		if (label.eContainer() instanceof RotationWrapper)
			return;

		final HashMultimap<String, Port> portsMappedToName = HashMultimap
				.create();

		Iterable<Port> allPortsWithNames = filter(
				getAllContentsOfType(label, Port.class),
				port -> port.getName() != null);

		for (final Port port : allPortsWithNames) {
			portsMappedToName.put(port.getName(), port);
		}

		/*
		 * No two ports are allowed the same name, the multimap's values are
		 * grouped by key (asMap), then filtered by collection size. All
		 * remaining ports shall yield an error.
		 */
		Iterable<Port> allMisnamedPorts = concat(
				filter(portsMappedToName.asMap().values(),
						collection -> collection.size() > 1));

		for (final Port misnamedPort : allMisnamedPorts) {
			error("Port name not unique: " + misnamedPort.getName(),
					misnamedPort.eContainer(),
					RecordlabelPackage.eINSTANCE.getBaseField_Port(),
					PORT_NAME_DUPLICATE);
		}
	}
}
