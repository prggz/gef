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
package org.eclipse.gef.dot.tests

import com.google.inject.Inject
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.gef.dot.internal.language.DotRecordLabelInjectorProvider
import org.eclipse.gef.dot.internal.language.recordlabel.AbstractField
import org.eclipse.gef.dot.internal.language.recordlabel.BaseField
import org.eclipse.gef.dot.internal.language.recordlabel.Port
import org.eclipse.gef.dot.internal.language.recordlabel.RecordLabel
import org.eclipse.gef.dot.internal.language.recordlabel.RecordlabelFactory
import org.eclipse.gef.dot.internal.language.recordlabel.RecordlabelPackage
import org.eclipse.gef.dot.internal.language.recordlabel.RotationWrapper
import org.eclipse.gef.dot.internal.language.validation.DotRecordLabelJavaValidator
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith

import static org.junit.Assert.*

@RunWith(XtextRunner)
@InjectWith(DotRecordLabelInjectorProvider)
public class DotRecordLabelTests {
	@Inject extension ParseHelper<RecordLabel> parseHelper
	@Inject extension ValidationTestHelper

	// good Syntax
	@Test def void emptyString() {
		''''''.assertNoErrors.assertTreeEquals(
			label(baseField(null))
		)
	}

	@Test def void singleLetter() {
		'''F'''.assertNoErrors.assertTreeEquals(
			label(baseField("F"))
		)
	}

	@Test def void specialSign() {
		'''§'''.assertNoErrors.assertTreeEquals(
			label(baseField("§"))
		)
	}

	@Test def void word() {
		'''Hello'''.assertNoErrors.assertTreeEquals(
			label(baseField("Hello"))
		)
	}

	@Test def void escapedCharacter() {
		'''Please\ read\ §146'''.assertNoErrors.assertTreeEquals(
			label(baseField('''Please\ read\ §146'''))
		)
	}

	@Test def void escapedBraceInText() {
		'''Ple\}se146read'''.assertNoErrors.assertTreeEquals(
			label(baseField('''Ple\}se146read'''))
		)
	}

	@Test def void escapedBackslash() {
		'''\\'''.assertNoErrors.assertTreeEquals(
			label(baseField('''\\'''))
		)
	}

	@Test def void whiteSpaceBetweenLetters() {
		'''k D'''.assertNoErrors.assertTreeEquals(
			label(baseField('''k D'''))
		)
	}

	@Test def void separatorSign() {
		'''abc|def'''.assertNoErrors.assertTreeEquals(
			label(
				baseField("abc"),
				baseField("def")
			)
		)
	}

	@Test def void threeFields() {
		'''abc | def | gh4i'''.assertNoErrors.assertTreeEquals(
			label(
				baseField("abc"),
				baseField("def"),
				baseField("gh4i")
			)
		)
	}

	@Test def void simpleFourFields() {
		'''A | B | C | D'''.assertNoErrors.assertTreeEquals(
			label(baseField("A"), baseField("B"), baseField("C"), baseField("D")))
	}

	@Test def void emptyRotatedLabel() {
		'''{}'''.assertNoErrors.assertTreeEquals(
			label(rotationWrapper(label(
				baseField(null)
			)))
		)
	}

	@Test def void simpleRotation() {
		'''{ Hi }'''.assertNoErrors.assertTreeEquals(
			label(rotationWrapper(label(
				baseField("Hi")
			)))
		)
	}

	@Test def void rotatedFourFieldsLabel() {
		'''{ Hi | This | Is | Awesome }'''.assertNoErrors.assertTreeEquals(
			label(rotationWrapper(label(
				baseField("Hi"),
				baseField("This"),
				baseField("Is"),
				baseField("Awesome")
			)))
		)
	}

	@Test def void rotatedMoreComplexLabel() {
		'''Hi | {Test | Section 2 } | xyz'''.assertNoErrors.assertTreeEquals(
			label(
				baseField("Hi"),
				rotationWrapper(label(
					baseField("Test"),
					baseField("Section 2")
				)),
				baseField("xyz")
			)
		)
	}

	@Test def void fieldId() {
		'''<fgh> someField'''.assertNoErrors.assertTreeEquals(
			label(baseField(port("fgh"), "someField"))
		)
	}

	@Test def void emptyPortname() {
		'''<>'''.assertNoErrors.assertTreeEquals(
			label(
				baseField(port(null), null)
			)
		)
	}

	@Test def void emptyPortnameWithText() {
		'''<> kids'''.assertNoErrors.assertTreeEquals(
			label(
				baseField(port(null), "kids")
			)
		)
	}

	@Test def void namedPort() {
		'''<Label>'''.assertNoErrors.assertTreeEquals(
			label(baseField(port("Label"), null))
		)
	}

	@Test def void portInHField() {
		'''{<Label>}'''.assertNoErrors.assertTreeEquals(
			label(rotationWrapper(
				label(baseField(port("Label"), null))
			))
		)
	}

	@Test def void portInHFieldWithText() {
		'''{<Label> Coolstuff!}'''.assertNoErrors.assertTreeEquals(
			label(rotationWrapper(
				label(baseField(port("Label"), "Coolstuff!"))
			))
		)
	}

	@Test def void portWithEscapedCharInName() {
		'''<some_weans\{>'''.assertNoErrors.assertTreeEquals(
			label(
				baseField(port('''some_weans\{'''), null)
			)
		)
	}

	// complex Parse Tests
	@Test def void parseTreeSimple() {
		'''hello word | <port> cool stuff going on '''.assertTreeEquals(label(
			baseField("hello word"),
			baseField(port("port"), "cool stuff going on")
		))
	}

	@Test def void parseTreeComplex() {
		'''
		hello word | cool stuff going on | { <free> free beer here |
		wine there } | sad it's just a test'''.assertTreeEquals(
			label(baseField("hello word"), baseField("cool stuff going on"), rotationWrapper(
				label(
					baseField(port("free"), "free beer here"),
					baseField("wine there")
				)
			), baseField("sad it's just a test"))
		)
	}

	@Test def void documentationExampleLine1() {
		'''<f0> left|<f1> mid&#92; dle|<f2> right'''.assertNoErrors.assertTreeEquals(
			label(
				baseField(port("f0"), "left"),
				baseField(port("f1"), "mid&#92; dle"),
				baseField(port("f2"), "right")
			)
		)
	}

	@Test def void documentationExampleLine3() {
		'''hello&#92;nworld |{ b |{c|<here> d|e}| f}| g | h'''.assertNoErrors.assertTreeEquals(
			label(
				baseField("hello&#92;nworld"),
				rotationWrapper(label(
					baseField("b"),
					rotationWrapper(label(
						baseField("c"),
						baseField(port("here"), "d"),
						baseField("e")
					)),
					baseField("f")
				)),
				baseField("g"),
				baseField("h")
			)
		)
	}

	@Test def void complexExampleLineBreak() {
		'''
		hello&#92;nworld |{ b |{c|<here>
		 d
		 |e}| f}|
		g | h'''.assertNoErrors.assertTreeEquals(
			label(
				baseField("hello&#92;nworld"),
				rotationWrapper(label(
					baseField("b"),
					rotationWrapper(label(
						baseField("c"),
						baseField(port("here"), "d"),
						baseField("e")
					)),
					baseField("f")
				)),
				baseField("g"),
				baseField("h")
			)
		)
	}

	@Test def void complexLineBreakInString() {
		'''
		hello
		world |{ b |{c|<here>
		 d|e}| f}|
		g | h'''.assertNoErrors.assertTreeEquals(
			label(
				baseField('''
				hello
				world'''),
				rotationWrapper(label(
					baseField("b"),
					rotationWrapper(label(
						baseField("c"),
						baseField(port("here"), "d"),
						baseField("e")
					)),
					baseField("f")
				)),
				baseField("g"),
				baseField("h")
			)
		)
	}

	@Test def void complexExampleUsingSpecialSignsRotated() {
		'''{Animal|+ name : string\l+ age : int\l|+ die() : void\l}'''.assertNoErrors.assertTreeEquals(
			label(rotationWrapper(label(
				baseField("Animal"),
				baseField('''+ name : string\l+ age : int\l'''),
				baseField('''+ die() : void\l''')
			)))
		)

	}

	@Test def void baseFieldsWithNoEntry() {
		'''<f0> (nil)| | |-1'''.assertNoErrors.assertTreeEquals(
			label(
				baseField(port("f0"), "(nil)"),
				baseField(null),
				baseField(null),
				baseField("-1")
			)
		)
	}

	// bad Syntax
	@Test def void singleClosePortFails() { '''>'''.assertSyntaxErrorLabel(">") }

	@Test def void singleCloseBraceFails() { '''}'''.assertSyntaxErrorLabel("}") }

	@Test def void missingOpenBraceFails() { '''}asas'''.assertSyntaxErrorLabel("}") }

	@Test def void escapedOpeningBraceFails() { '''\{ Hello }'''.assertSyntaxErrorLabel("}") }

	@Test def void escapedClosingBraceFails() { '''{ Hello \}'''.assertSyntaxErrorBaseField("<EOF>") }

	@Test def void escapedOpeningPortFails() { '''\< Hello >'''.assertSyntaxErrorLabel(">") }

	@Test def void escapedClosingPortFails() { '''< Hello \>'''.assertSyntaxErrorPort("<EOF>") }

	@Test def void missingClosingPortFails() { '''< Hello'''.assertSyntaxErrorPort("<EOF>") }

	@Test def void portWithBraceFails() { '''< Hello }>'''.assertSyntaxErrorPort(">") }

	@Test def void braceUnclosedFirstFieldFails() { '''{ Hello | MoreHi'''.assertSyntaxErrorBaseField("<EOF>") }

	@Test def void braceUnclosedSecondFieldFails() { '''hello|{ hslnh'''.assertSyntaxErrorBaseField("<EOF>") }

	@Test def void wrongPosLabelFails() { '''sdsdsdsd<>'''.assertSyntaxErrorLabel("<") }

	@Test def void bracesInFieldFail() { '''This{Is}Illegal'''.assertSyntaxErrorLabel("{") }

	@Test def void bracesInMiddleFail() { '''This{Is}Illegal'''.assertSyntaxErrorLabel("{") }

	@Test def void bracesAfterPortNameFail() { '''<Port1>{Stuff}'''.assertSyntaxErrorLabel("{") }

	@Test def void complexBracesMistaken() { '''<f0> left|{ middle|<f2> right} boo'''.assertSyntaxErrorLabel("boo") }

	@Test def void missingABraceMiddle() {
		'''
		hello word | cool stuff going on | { <free> free beer here |
		<expensive wine there } | sad its just a test'''.assertSyntaxErrorRotationWrapper(">")
	}

	// validation tests
	@Test
	def void sameNamePortsSameLevel() {
		'''<here>|<here>'''.assertValidationErrorBaseField(DotRecordLabelJavaValidator.PORT_NAME_DUPLICATE)
	}

	@Test
	def void sameNamePortsDifferentLevel() {
		'''a | <b> c | { <d> f | <b> f } | x'''.assertValidationErrorBaseField(
			DotRecordLabelJavaValidator.PORT_NAME_DUPLICATE,
			4,
			3
		).assertValidationErrorBaseField(
			DotRecordLabelJavaValidator.PORT_NAME_DUPLICATE,
			22,
			3
		)
	}

	@Test
	def void twoEmptyPortNamesNoError() {
		'''<> a | <> b'''.assertNoErrors()
	}

	@Test
	def void emptyPortNameWarning() {
		'''<>'''.parse.assertWarning(
			RecordlabelPackage.eINSTANCE.port,
			DotRecordLabelJavaValidator.PORT_NAME_NOT_SET
		)
	}

	@Test
	def void complexEmptyPortNameWarning() {
		'''a | <b> c | { <d> f | <> f } | x'''.parse.assertWarning(
			RecordlabelPackage.eINSTANCE.port,
			DotRecordLabelJavaValidator.PORT_NAME_NOT_SET
		)
	}

	@Test
	def void noWhitespaceWarning() {
		'''a | <b> coolstuff | { <d> f\ kinds | <f> f\nbut } | x'''.assertNoIssues
	}

	private def CharSequence assertValidationErrorBaseField(CharSequence content, String error, int offset,
		int length) {
		assertError(parse(content), RecordlabelPackage.eINSTANCE.baseField, error, offset, length)
		content
	}

	private def CharSequence assertValidationErrorBaseField(CharSequence content, String error) {
		assertError(parse(content), RecordlabelPackage.eINSTANCE.baseField, error)
		return content
	}

	private def CharSequence assertNoIssues(CharSequence sequence) {
		sequence.parse.assertNoIssues
		return sequence
	}

	private def CharSequence assertNoErrors(CharSequence sequence) {
		sequence.parse.assertNoErrors
		return sequence
	}

	private def CharSequence assertSyntaxErrorLabel(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.recordLabel, "'" + character + "'")
	}

	private def CharSequence assertSyntaxErrorRotationWrapper(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.rotationWrapper, "'" + character + "'")
	}

	private def CharSequence assertSyntaxErrorBaseField(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.baseField, "'" + character + "'")
	}

	private def CharSequence assertSyntaxErrorPort(CharSequence content, String character) {
		return assertSyntaxError(content, RecordlabelPackage.eINSTANCE.port, "'" + character + "'")
	}

	private def CharSequence assertSyntaxError(CharSequence content, EClass eClass, String message) {
		assertError(parse(content), eClass, "org.eclipse.xtext.diagnostics.Diagnostic.Syntax", message)
		return content
	}

	private def void assertTreeEquals(CharSequence sequenceForParsing, EObject expected) {
		sequenceForParsing.parse.assertTreeEquals(expected)
	}

	private def EObject assertTreeEquals(EObject actual, EObject expected) {
		assertEquals("Objects of different classtype ", expected.eClass, actual.eClass)
		for (attribute : expected.eClass.EAllAttributes) {
			assertEquals("Attribute " + attribute.name + " of class " + expected.eClass.name, expected.eGet(attribute),
				actual.eGet(attribute))
		}
		assertEquals("Number of Child Nodes", expected.eContents.size, actual.eContents.size)
		for (var i = 0; i < expected.eContents.size; i++) {
			actual.eContents.get(i).assertTreeEquals(expected.eContents.get(i))
		}
		return actual
	}

	private def RecordLabel label(AbstractField... fields) {
		val label = RecordlabelFactory.eINSTANCE.createRecordLabel
		label.fields.addAll(fields)
		return label
	}

	private def BaseField baseField(Port port, String name) {
		val baseField = RecordlabelFactory.eINSTANCE.createBaseField
		baseField.port = port
		baseField.name = name
		return baseField
	}

	private def BaseField baseField(String name) {
		baseField(null, name)
	}

	private def Port port(String name) {
		val port = RecordlabelFactory.eINSTANCE.createPort
		port.name = name
		return port
	}

	private def RotationWrapper rotationWrapper(RecordLabel label) {
		val wrapper = RecordlabelFactory.eINSTANCE.createRotationWrapper
		wrapper.label = label
		return wrapper
	}
}
