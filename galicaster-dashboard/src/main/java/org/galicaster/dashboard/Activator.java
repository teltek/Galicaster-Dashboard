/**
 * TODO add license
 */

package org.galicaster.dashboard;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

  /**
   * the logging facility provided by log4j
   */
  private static final Logger logger = LoggerFactory.getLogger(Activator.class);

  @Override
  public void start(BundleContext context) throws Exception {
    logger.info("Starting Galicaster Dashboard");
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    logger.info("Stopped Galicaster Dashboard");
  }
}
