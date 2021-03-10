package edu.kit.ipd.parse.changeWD;

import edu.kit.ipd.parse.luna.ILuna;
import edu.kit.ipd.parse.luna.Luna;
import edu.kit.ipd.parse.luna.event.AbortEvent;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.ParseGraph;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import org.junit.After;
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

	private ChangeWatchdog cwd;

	@Before
	public void beforeTest() {
		cwd = new ChangeWatchdog();
	}

	@After
	public void tearDown() {
		Luna.tearDown();
	}

	/**
	 * Tests whether the agent has added the term node type if the graph remains
	 * unaltered after the timeout exceeded.
	 */
	@Test
	public void testExec4000msNoChange() {
		ILuna luna = Luna.getInstance();
		wdProps.setProperty("CHANGE_TIMEOUT_THRESHOLD", "4000");
		luna.register(cwd);
		cwd.initAbstract(luna);
		long start = System.currentTimeMillis();
		long curr = start;
		while (curr - start < 4001) {
			cwd.setGraph(luna.getMainGraph());
			cwd.exec();
			curr = System.currentTimeMillis();
		}
		assertTrue(cwd.getCurrEvent() instanceof AbortEvent);
	}

	/**
	 * Tests whether the agent doesn't add the term node before the timeout is
	 * exceeded and the graph remains unaltered.
	 */
	@Test
	public void testExec4000msNoChangeFail() {
		ILuna luna = Luna.getInstance();
		wdProps.setProperty("CHANGE_TIMEOUT_THRESHOLD", "4000");
		luna.register(cwd);
		cwd.initAbstract(luna);
		long start = System.currentTimeMillis();
		long curr = start;
		assertFalse(cwd.getCurrEvent() instanceof AbortEvent);
		while (curr - start < 3900) {
			cwd.setGraph(luna.getMainGraph());
			cwd.exec();
			curr = System.currentTimeMillis();
		}
		assertFalse(cwd.getCurrEvent() instanceof AbortEvent);
	}

	/**
	 * Tests whether the agent doesn't add the term node after 7 consecutive graph
	 * changes (with a timeout of 500) and has added the term node type after
	 * another timeout of 2500 without a change to the graph.
	 */
	@Test
	public void testMultExec5000ms() {
		ILuna luna = Luna.getInstance();
		wdProps.setProperty("CHANGE_TIMEOUT_THRESHOLD", "2000");
		luna.register(cwd);
		cwd.initAbstract(luna);
		for (int i = 0; i < 7; i++) {
			luna.getMainGraph().createNodeType(String.valueOf(i));
			long start = System.currentTimeMillis();
			long curr = start;
			while (curr - start < 500) {
				cwd.setGraph(luna.getMainGraph());
				cwd.exec();
				curr = System.currentTimeMillis();
			}
			assertFalse(cwd.getCurrEvent() instanceof AbortEvent);
		}
		long start = System.currentTimeMillis();
		long curr = start;
		while (curr - start < 2500) {
			cwd.exec();
			curr = System.currentTimeMillis();
		}
		assertTrue(cwd.getCurrEvent() instanceof AbortEvent);
	}
}