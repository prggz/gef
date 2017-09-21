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
package org.eclipse.gef.dot.internal.language;

import org.eclipse.gef.dot.internal.language.recordlabel.DotRecordLabelTerminalConverters;
import org.eclipse.xtext.conversion.IValueConverterService;

/**
 * Use this class to register components to be used at runtime / without the
 * Equinox extension registry.
 */
public class DotRecordLabelRuntimeModule extends
		org.eclipse.gef.dot.internal.language.AbstractDotRecordLabelRuntimeModule {

	@Override
	public Class<? extends IValueConverterService> bindIValueConverterService() {
		return DotRecordLabelTerminalConverters.class;
	}
}
