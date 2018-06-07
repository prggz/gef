/*******************************************************************************
 * Copyright (c) 2018 itemis AG and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Zoey Gerrit Prigge (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef.dot.tests

import com.google.inject.Inject
import org.eclipse.gef.dot.internal.DotImport
import org.eclipse.gef.dot.internal.language.DotInjectorProvider
import org.eclipse.gef.dot.internal.language.dot.DotAst
import org.eclipse.gef.dot.internal.ui.Dot2ZestAttributesConverter
import org.eclipse.gef.fx.nodes.GeometryNode
import org.eclipse.gef.geometry.planar.Ellipse
import org.eclipse.gef.geometry.planar.IGeometry
import org.eclipse.gef.geometry.planar.Polygon
import org.eclipse.gef.geometry.planar.Rectangle
import org.eclipse.gef.graph.Node
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

import static extension org.eclipse.gef.zest.fx.ZestProperties.*
import static extension org.junit.Assert.*

/*
 * Test cases for the {@link Dot2ZestAttributesConverter#convertAttributes(Node, Node)} method.
 * 
 * TODO Implement Tests for:
 * - pos Attribute
 * - html Labels
 * - record based label visualization
 * - xlp
 */
@RunWith(XtextRunner)
@InjectWith(DotInjectorProvider)
class Dot2ZestNodeAttributesConversionTests {
	
	@Inject extension ParseHelper<DotAst>
	@Inject extension ValidationTestHelper
	
	extension DotImport = new DotImport
	static extension Dot2ZestAttributesConverter converter = new Dot2ZestAttributesConverter
	
	@BeforeClass
	def static void before() {
		DotTestUtils.registerDotSubgrammarPackages
		converter.options.emulateLayout=false //TODO remove once FX tests work
	}

	@Test def node_height001() {
		'''
			digraph {
				1[fixedsize=true]
			}
		'''.assertNodeHeight(0.5*72)
	}
	
	@Test def node_height002() {
		'''
			digraph {
				1[height="0.4"]
			}
		'''.assertNodeHeight(0.4*72)
	}
	
	@Test def node_width001() {
		'''
			digraph {
				1[width="1"]
			}
		'''.assertNodeWidth(1.0*72)
	}
	
	@Test def node_width002() {
		'''
			digraph {
				1[width="0.4"]
			}
		'''.assertNodeWidth(0.4*72)
	}
	
	@Test def node_width003() {
		'''
			digraph {
				1
			}
		'''.assertNodeWidth(0.75*72)
	}
	
	@Test def node_label001() {
		'''
			digraph {
				1
			}
		'''.assertNodeLabel("1")
	}
	
	@Test def node_label002() {
		'''
			digraph {
				1
			}
		'''.assertNodeLabel("1")
	}
	
	@Test def node_label003() {
		'''
			digraph {
				2->3
			}
		'''.assertNodeLabel("2")
	}
	
	@Test def node_label004() {
		'''
			digraph {
				fun
			}
		'''.assertNodeLabel("fun")
	}
	
	@Test def node_label005() {
		'''
			digraph {
				1[label="label"]
			}
		'''.assertNodeLabel("label")
	}
	
	@Test def node_label006() {
		'''
			digraph {
				1[label="label \N"]
			}
		'''.assertNodeLabel("label 1")
	}
	
	@Test def node_label007() {
		'''
			digraph {
				sample [label="\N"]
			}
		'''.assertNodeLabel("sample")
	}
	
	@Test def node_label008() {
		'''
			graph mygraph {
				a[label="graph: \G, node no. \N"]
			}
		'''.assertNodeLabel("graph: mygraph, node no. a")
	}
	
	@Test def node_label009() {
		//test to ascertain no loop is reached 
		'''
			graph {
				a[label="\L"]
			}
		'''.assertNodeLabel("\\L")
	}
	
	@Test def node_label010() {
		//test to ascertain no NPE is reached 
		'''
			graph {
				a[label="\G"]
			}
		'''.assertNodeLabel("")
	}
	
	@Test def node_id001() {
		'''
			digraph {
				1
			}
		'''.assertNodeId(null)
	}

	@Test def node_id002() {
		'''
			digraph {
				cool->jessie
			}
		'''.assertNodeId(null)
	}
	
	@Test def node_id003() {
		'''
			digraph {
				cool[id="stuff"]
			}
		'''.assertNodeId("stuff")
	}
	
	@Test def node_xlabel001() {
		'''
			digraph {
				1
			}
		'''.assertNodeXLabel(null)
	}
	
	@Test def node_xlabel002() { 
		'''
			digraph {
				1[xlabel="fantastic label"]
			}
		'''.assertNodeXLabel("fantastic label")
	}
	
	@Test def node_xlabel003() { 
		'''
			digraph testedGraphName {
				1[xlabel="node:\L graph:\G"]
			}
		'''.assertNodeXLabel("node:1 graph:testedGraphName")
	}
	
	@Test def node_polygonbasedshape001() { 
		'''
			digraph {
				1[shape=box]
			}
		'''.assertNodePolygonBasedShape(new Rectangle)
	}
	
	@Test def node_polygonbasedshape002() { 
		'''
			digraph {
				1[shape=rect]
			}
		'''.assertNodePolygonBasedShape(new Rectangle)
	}
	
	@Test def node_polygonbasedshape003() {
		'''
			digraph{1[shape=rectangle]}
		'''.assertNodePolygonBasedShape(new Rectangle)
	}
	
	@Test def node_polygonbasedshape004() {
		'''
			digraph{1[shape=square]}
		'''.assertNodePolygonBasedShape(new Rectangle)
	}
	
	@Test def node_polygonbasedshape007() {
		'''
			digraph{1[shape=cds]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 100, 0, 0, 70, 0, 100, 50, 70, 100))
	}
	
	@Test def node_polygonbasedshape008() {
		'''
			digraph{1[shape=ellipse]}
		'''.assertNodePolygonBasedShape(new Ellipse(new Rectangle))
	}
	
	@Test def node_polygonbasedshape009() {
		'''
			digraph{1[shape=circle]}
		'''.assertNodePolygonBasedShape(new Ellipse(new Rectangle))
	}
	
	@Test def node_polygonbasedshape010() {
		'''
			digraph{1[shape=oval]}
		'''.assertNodePolygonBasedShape(new Ellipse(new Rectangle))
	}
	
	@Test def node_polygonbasedshape011() {
		'''
			digraph{1[shape=point]}
		'''.assertNodePolygonBasedShape(new Ellipse(new Rectangle))
	}
	
	@Test def node_polygonbasedshape012() {
		'''
			digraph{1[shape=diamond]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 50, 50, 0, 100, 50, 50, 100))
	}
	
	@Test def node_polygonbasedshape013() {
		'''
			digraph{1[shape=folder]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 100, 0, 10, 50, 10, 55, 0, 95, 0, 100, 10, 100, 100))
	}
	
	@Test def node_polygonbasedshape014() {
		'''
			digraph{1[shape=house]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 100, 0, 40, 50, 0, 100, 40, 100, 100))
	}
	
	@Test def node_polygonbasedshape015() {
		'''
			digraph{1[shape=invhouse]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 0, 100, 0, 100, 60, 50, 100, 0, 60))
	}
	
	@Test def node_polygonbasedshape016() {
		'''
			digraph{1[shape=invtrapezium]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 0, 100, 0, 75, 100, 25, 100))
	}
	
	@Test def node_polygonbasedshape017() {
		'''
			digraph{1[shape=invtriangle]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 10, 100, 10, 50, 100))
	}
	
	@Test def node_polygonbasedshape018() {
		'''
			digraph{1[shape=hexagon]}
		'''.assertNodePolygonBasedShape(new Polygon(25, 100, 0, 50, 25, 0, 75, 0, 100, 50, 75, 100))
	}
	
	@Test def node_polygonbasedshape019() {
		'''
			digraph{1[shape=larrow]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 50, 40, 0, 40, 15, 100, 15, 100, 85, 40, 85, 40, 100))
	}
	
	@Test def node_polygonbasedshape020() {
		'''
			digraph{1[shape=lpromoter]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 50, 40, 0, 40, 15, 100, 15, 100, 100, 70, 100, 70, 85, 40, 85, 40, 100))
	}
	
	@Test def node_polygonbasedshape021() {
		'''
			digraph{1[shape=octagon]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 70, 0, 30, 30, 0, 70, 0, 100, 30, 100, 70, 70, 100, 30, 100))
	}
	
	@Test def node_polygonbasedshape022() {
		'''
			digraph{1[shape=parallelogram]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 100, 25, 0, 100, 0, 75, 100))
	}
	
	@Test def node_polygonbasedshape023() {
		'''
			digraph{1[shape=pentagon]}
		'''.assertNodePolygonBasedShape(new Polygon(25, 100, 0, 40, 50, 0, 100, 40, 75, 100))
	}
	
	@Test def node_polygonbasedshape024() {
		'''
			digraph{1[shape=rarrow]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 85, 0, 15, 60, 15, 60, 0, 100, 50, 60, 100, 60, 85))
	}
	
	@Test def node_polygonbasedshape025() {
		'''
			digraph{1[shape=rpromoter]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 100, 0, 15, 60, 15, 60, 0, 100, 50, 60, 100, 60, 85, 30, 85, 30, 100))
	}
	
	@Test def node_polygonbasedshape026() {
		'''
			digraph{1[shape=septagon]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 60, 15, 15, 50, 0, 85, 15, 100, 60, 75, 100, 25, 100))
	}
	
	@Test def node_polygonbasedshape027() {
		'''
			digraph{1[shape=star]}
		'''.assertNodePolygonBasedShape(new Polygon(15, 100, 30, 60, 0, 40, 40, 40, 50, 0, 60, 40, 100, 40, 70, 60, 85, 100, 50, 75))
	}
	
	@Test def node_polygonbasedshape028() {
		'''
			digraph{1[shape=trapezium]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 100, 25, 0, 75, 0, 100, 100))
	}
	
	@Test def node_polygonbasedshape029() {
		'''
			digraph{1[shape=triangle]}
		'''.assertNodePolygonBasedShape(new Polygon(0, 50, 50, 0, 100, 50))
	}
	
	@Test def node_polygonbasedshape030() {
		'''
			digraph{1}
		'''.assertNodePolygonBasedShape(new Ellipse(new Rectangle))
	}
	
	@Test def node_style001() {
		'''
			digraph{
				1
			}
		'''.assertNodeStyle("")
	}
	
	@Test def node_style002() {
		'''
			digraph{
				1[color=red]
			}
		'''.assertNodeStyle('''
			-fx-stroke: #ff0000;
		''')
	}
	
	@Test def node_style003() {
		'''
			digraph{
				1[style=bold]
			}
		'''.assertNodeStyle('''
			-fx-stroke-width:2;
		''')
	}
	
	@Test def node_style004() {
		'''
			digraph{
				1[style=solid]
			}
		'''.assertNodeStyle('''
			-fx-stroke-width: 1;
		''')
	}
	
	@Test def node_style005() {
		'''
			digraph{
				1[style=dashed]
			}
		'''.assertNodeStyle('''
			-fx-stroke-dash-array: 7 7;
		''')
	}
	
	@Test def node_style006() {
		'''
			digraph{
				1[style=dotted]
			}
		'''.assertNodeStyle('''
			-fx-stroke-dash-array: 1 6;
		''')
	}
	
	@Test def node_fill001() {
		'''
			digraph{
				1[color="red", style=filled]
			}
		'''.assertNodeStyle('''
			-fx-stroke: #ff0000;
			-fx-fill: #ff0000;
		''')
	}
	
	@Test def node_colorscheme001() { //The aqua color is not part of the default x11 colorscheme
		'''
			digraph{
				1[colorscheme=svg, fillcolor="aqua", style=filled]
			}
		'''.assertNodeStyle('''
			-fx-fill: #00ffff;
		''')
	}	
	
	@Test def node_colorscheme002() { //The aqua color is not part of the default x11 colorscheme
		'''
			digraph{
				1[colorscheme=svg, color="aqua"]
			}
		'''.assertNodeStyle('''
			-fx-stroke: #00ffff;
		''')
	}
	
	@Test def node_visibility001() {
		'''
			digraph{
				1
			}
		'''.assertNodeVisibility(true)
	}
	
	@Test def node_visibility002() {
		'''
			digraph{
				1[style=invis]
			}
		'''.assertNodeVisibility(false)
	}
	
	@Test def node_tooltip001() {
		'''
			digraph{
				1[tooltip="test"]
			}
		'''.assertNodeTooltip("test")
	}
	
	@Test def node_tooltip002() {
		'''
			digraph{
				1[tooltip="testing\nis\nfun"]
			}
		'''.assertNodeTooltip("testing\nis\nfun")
	}
	
	@Test def node_tooltip003() {
		'''
			digraph testing{
				nodename[label="label of \N", tooltip="l:\L n:\N g:\G"]
			}
		'''.assertNodeTooltip("l:label of nodename n:nodename g:testing")
	}
			
	private def assertNodeWidth(CharSequence dotText, double expected) {
		val actual = dotText.firstNode.convert.size.width
		expected.assertEquals(actual, 0)
	}
	
	private def assertNodeHeight(CharSequence dotText, double expected) {
		val actual = dotText.firstNode.convert.size.height
		expected.assertEquals(actual, 0)
	}
	
	private def assertNodeId(CharSequence dotText, String expected) {
		val actual = dotText.firstNode.convert.cssId
		expected.assertEquals(actual)
	}
	
	private def assertNodeLabel(CharSequence dotText, String expected) {
		val actual = dotText.firstNode.convert.label
		expected.assertEquals(actual)
	}
	
	private def assertNodeXLabel(CharSequence dotText, String expected) {
		val actual = dotText.firstNode.convert.externalLabel
		expected.assertEquals(actual)
	}
		
	private def assertNodePolygonBasedShape(CharSequence dotText, IGeometry expected) {
		val zestShape = dotText.firstNode.convert.shape
		if (zestShape instanceof GeometryNode<?>) {
			val actual = zestShape.geometry
			expected.assertEquals(actual)
		}
		else fail("GeometryNode expected, got " + zestShape.class)
	}
	
	private def assertNodeStyle(CharSequence dotText, String expected) {
		val actual = dotText.firstNode.convert.shape.style
		expected.assertEquals(actual.split)
	}
	
	private def assertNodeTooltip(CharSequence dotText, String expected) {
		val actual = dotText.firstNode.convert.tooltip
		expected.assertEquals(actual)
	}
	
	private def assertNodeVisibility(CharSequence dotText, boolean expected) {
		val invisible = dotText.firstNode.convert.invisible
		// assert on visibility, not on invisibility
		val actual = if(invisible===null) true else !invisible
		expected.assertEquals(actual)
	}
	
	private def firstNode(CharSequence dotText) {
		dotText.graph.nodes.head
	}
	
	private def graph(CharSequence dotText) {
		// ensure that the input text can be parsed and the ast can be created
		val dotAst = dotText.parse
		dotAst.assertNoErrors
		
		dotAst.importDot.get(0)
	}
	
	private def convert(Node dotNode) {
		val zestNode = new Node
		dotNode.copy(zestNode)
		zestNode
	}

	private def split(String text) {
		text.replaceAll(";", ";" + System.lineSeparator)
	}
}