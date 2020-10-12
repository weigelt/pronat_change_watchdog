package edu.kit.ipd.parse.changeWD;

import edu.kit.ipd.parse.luna.AbstractLuna;
import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.agent.AbstractWatchdog;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import org.kohsuke.MetaInfServices;

import java.util.Properties;

/**
 * @author Sebastian Weigelt
 *
 */

@MetaInfServices(AbstractAgent.class)
public class ChangeWatchdog extends AbstractWatchdog {

	private static final String ID = "changeWatchdog";
	private static final Properties wdProps = ConfigManager.getConfiguration(ChangeWatchdog.class);
	private static final Properties lunaProps = ConfigManager.getConfiguration(AbstractLuna.class);

	private static final String PROP_TIMEOUT_THRESHOLD = "CHANGE_TIMEOUT_THRESHOLD";
	private static final String PROP_TERM_SIGNAL_TYPE = "TERM_SIGNAL_TYPE";

	private String termSignalType;
	private long cto_threshold;
	private long lastChangeTime = -1;
	private long lastGraphHash = -1;

	public ChangeWatchdog() {
		setId(ID);
	}

	/**
	 * 
	 */
	@Override
	public void init() {
		cto_threshold = Long.parseLong(wdProps.getProperty(PROP_TIMEOUT_THRESHOLD));
		termSignalType = lunaProps.getProperty(PROP_TERM_SIGNAL_TYPE);
	}

	/**
	 * 
	 */
	@Override
	protected void exec() {

		long currHash = graph.hashCode();
		if (lastGraphHash != currHash) {
			lastChangeTime = System.currentTimeMillis();
		} else {
			long currTime = System.currentTimeMillis();
			if (currTime - lastChangeTime > cto_threshold) {
				graph.createNodeType(termSignalType);
			}
		}
		lastGraphHash = currHash;
	}
}
