/*
 * Menu: testIncludeInBundleDefault
 * Kudos: James E. Ervin
 * License: EPL 1.0
 * Include-Bundle: org.apache.ant
 */
import net.sf.groovymonkey.tests.fixtures.dom.TestDOM
def ant = new AntBuilder()
ant.echo( "Hello there from AntBuilder" )

new TestDOM().callDOM( 'testIncludeInBundleDefault' )
