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

package org.eclipse.gef.dot.internal.language.recordlabel;

import org.eclipse.xtext.common.services.DefaultTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;

/**
 * Class Containing Terminal Converters for the RecordLabel sub grammar.
 */
public class DotRecordLabelTerminalConverters
		extends DefaultTerminalConverters {

	/**
	 * Returns a value converter for UNQUOTEDSTRING literals in the recordLabel
	 * grammar.
	 * 
	 * The value converter is needed to remove excess whitespace at the
	 * beginning and end of a UNQUOTEDSTRING literal. This is due to inability
	 * to remove such whitespace in the grammar itself.
	 * 
	 * @return The value converter for the UnquotedStrings.
	 */
	@ValueConverter(rule = "UNQUOTEDSTRING")
	public IValueConverter<String> UNQUOTEDSTRING() {
		return new IValueConverter<String>() {

			@Override
			public String toString(String value)
					throws ValueConverterException {
				return value;
			}

			@Override
			public String toValue(String string, INode node)
					throws ValueConverterException {
				return string.trim();
			}

		};
	}
}