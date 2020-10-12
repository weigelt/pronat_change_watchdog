package edu.kit.ipd.parse.changeWD;

import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Sebastian Weigelt
 * 
 */
public class ChangeWatchdogTest {

	private static final Properties wdProps = ConfigManager.getConfiguration(ChangeWatchdog.class);
	private static final Properties lunaProps = ConfigManager.getConfiguration(Luna.class);

	private IGraph graph;
	private ChangeWatchdog cwd;

	@BeforeClass
	public static void SetUp() {
		lunaProps.setProperty("TERM_SIGNAL_TYPE", "terminate");
	}

	@Before
	public void beforeTest() {
		graph = new ParseGraph();
		cwd = new ChangeWatchdog();
		cwd.init();
	}

	/**
	 * Tests whether the agent has added the term node type if the graph remains
	 * unaltered after the timeout exceeded.
	 */
	@Test
	public void testExec4000msNoChange() {

		wdProps.setProperty("TIMEOUT_THRESHOLD", "4000");
		cwd.setGraph(graph);
		cwd.exec();
		try {
			Thread.sleep(4100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cwd.setGraph(graph);
		cwd.exec();
		assertTrue(graph.hasNodeType("terminate"));
	}

	/**
	 * Tests whether the agent doesn't add the term node before the timeout is
	 * exceeded and the graph remains unaltered.
	 */
	@Test
	public void testExec5000msNoChangeFail() {

		wdProps.setProperty("TIMEOUT_THRESHOLD", "5000");
		cwd.setGraph(graph);
		cwd.exec();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cwd.setGraph(graph);
		cwd.exec();
		assertFalse(graph.hasNodeType("terminate"));
	}

	/**
	 * Tests whether the agent doesn't add the term node after 7 consecutive graph
	 * changes (with a timeout of 1000ms) and has added the term node type after
	 * another timeout of 6000ms without a change to the graph.
	 */
	@Test
	public void testMultExec5000ms() {

		wdProps.setProperty("TIMEOUT_THRESHOLD", "5000");
		for (int i = 0; i < 7; i++) {
			graph.createNodeType(String.valueOf(i));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cwd.setGraph(graph);
			cwd.exec();
			assertFalse(graph.hasNodeType("terminate"));
		}
		try {
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cwd.setGraph(graph);
		cwd.exec();
		assertTrue(graph.hasNodeType("terminate"));
	}
}