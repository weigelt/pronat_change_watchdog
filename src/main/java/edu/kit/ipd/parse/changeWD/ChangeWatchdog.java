package edu.kit.ipd.parse.changeWD;

import edu.kit.ipd.parse.luna.agent.AbstractAgent;
import edu.kit.ipd.parse.luna.agent.AbstractWatchdog;
import edu.kit.ipd.parse.luna.tools.ConfigManager;
import org.kohsuke.MetaInfServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author Sebastian Weigelt
 *
 */

@MetaInfServices(AbstractAgent.class)
public class ChangeWatchdog extends AbstractWatchdog {

	private static final String ID = "changeWatchdog";
	private static final Logger logger = LoggerFactory.getLogger(ChangeWatchdog.class);
	private static final Properties wdProps = ConfigManager.getConfiguration(ChangeWatchdog.class);

	private static final String PROP_TIMEOUT_THRESHOLD = "CHANGE_TIMEOUT_THRESHOLD";

	private long cto_threshold;
	private long currTime = -1;

	public ChangeWatchdog() {
		setId(ID);
	}

	/**
	 * Initializes the agent. Simply fetches the timeout threshold from the config
	 * file.
	 */
	@Override
	public void init() {
		cto_threshold = Long.parseLong(wdProps.getProperty(PROP_TIMEOUT_THRESHOLD));
	}

	/**
	 * 
	 */
	@Override
	protected void exec() {

		currTime = System.currentTimeMillis();

		if (checkTimeout()) {
			logger.info("Creating timeout signal. Last change was {} and now it is {}, which makes a diff of {}",
					graph.getLastChangedMillis(), currTime, (currTime - graph.getLastChangedMillis()));
			terminate();
		}
	}

	private boolean checkTimeout() {
		if (currTime - graph.getLastChangedMillis() >= cto_threshold) {
			return true;
		} else {
			return false;
		}
	}
}