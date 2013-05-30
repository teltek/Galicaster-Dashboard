/**
 * TODO add license
 */

package org.galicaster.dashboard;

//import static java.util.Arrays.asList;

import static org.opencastproject.security.api.SecurityConstants.GLOBAL_ADMIN_ROLE;

import org.opencastproject.capture.admin.api.Agent;
import org.opencastproject.capture.admin.api.CaptureAgentStateService;
import org.opencastproject.security.api.DefaultOrganization;
import org.opencastproject.security.api.SecurityService;
import org.opencastproject.security.api.User;
import org.opencastproject.util.NotFoundException;
import org.opencastproject.workspace.api.Workspace;

import org.apache.commons.io.IOUtils;
import org.galicaster.dashboard.snapshot.GstreamerSnapshotTaker;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Add description
 */
public class DashboardService {

  private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
  //private static final Set<String> goodStates = new HashSet<String>(asList(new String[]{AgentState.IDLE, AgentState.CAPTURING, AgentState.UPLOADING}));

  /** Maximum time (in milliseconds) that the service will wait for an agent to respond to ping */
  // TODO: Make this configurable
  private static final int PING_TIMEOUT = 5000; //ms

  /** Maximum time (in seconds) that the service will wait for the pipeline to take a snapshot */
  // TODO: Make this configurable
  private static final int SNAPSHOT_TIMEOUT = 10; //seconds

  /** Time between two subsequent updates of an agent's snapshot */
  // TODO: Make this configurable
  private static final long SNAPSHOT_DELAY = 60; //seconds

  /** Maximum number of snapshot extracting threads allowed to run that the same time */ 
  // TODO: Make this configurable
  private static final int DEFAULT_MAX_CONCURRENT = 5;

  /** This regular expression matches property names of the form "agent.[name].vnc.password", where
   * [name] is any agent name, that may not contain dots. The [name] contents are captured in group 1.
   */
  private static final String PASSWORD_REGEXP = "agent\\.([^.]+)\\.vnc\\.password";

  /** Name of the property that indicates the global default VNC password for the agents */
  private static final String DEFAULT_PASSWORD_PROPERTY = "default.vnc.password";

  /** Collection name in the workspace where the agent pictures are stored */
  private static final String COLLECTION_NAME = "dashboard-files";

  /** Directory to temporarily store the extracted images */
  private static final File tempDir = new File(System.getProperty("java.io.tmpdir"));

  /** Contains the VNC passwords specified in the config file */
  private Properties agentPasswords = new Properties();

  private ScheduledExecutorService periodicSnapshots;

  /** The default password, as set up in the configuration file, if present */
  private String defaultPassword;

  /** The system account to use for running asynchronous events */
  protected String systemAccount = null;

  /** The capture agent state service */
  protected CaptureAgentStateService captureAgentStateService;

  /** A reference to the workspace service */
  private Workspace workspace;

  /** A reference to the security service */
  private SecurityService securityService;


  protected void activate(ComponentContext cc) throws URISyntaxException {

    if (cc != null) {

      // Set security information
      this.systemAccount = cc.getBundleContext().getProperty("org.opencastproject.security.digest.user");

      DefaultOrganization defaultOrg = new DefaultOrganization();
      securityService.setOrganization(defaultOrg);
      securityService.setUser(new User(systemAccount, defaultOrg.getId(), new String[] { GLOBAL_ADMIN_ROLE }));


      @SuppressWarnings("unchecked")
      Dictionary<String, String> props = cc.getProperties();

      // Get the default VNC password, if it is defined in the config file
      defaultPassword = props.get(DEFAULT_PASSWORD_PROPERTY);
      if (defaultPassword == null)
        defaultPassword = "";

      // Get the passwords per agent, if specified in the config file
      Pattern propertyPattern = Pattern.compile(PASSWORD_REGEXP);



      for (Enumeration<String> keys = props.keys(); keys.hasMoreElements();) { 
        String key = keys.nextElement();
        Matcher match = propertyPattern.matcher(key);

        if (match.matches()) {
          agentPasswords.setProperty(match.group(1), props.get(match.group()));
        }
      }
    } 


    periodicSnapshots = Executors.newScheduledThreadPool(DEFAULT_MAX_CONCURRENT);

    Map<String, Agent> knownAgents = captureAgentStateService.getKnownAgents();

    for (Map.Entry<String, Agent> entry : knownAgents.entrySet()) {
      SnapshotWithDeadline task = new SnapshotWithDeadline(entry.getValue(), tempDir,
              agentPasswords.getProperty(entry.getKey(), defaultPassword), SNAPSHOT_TIMEOUT, TimeUnit.SECONDS);

      // TODO: Change start time
      periodicSnapshots.scheduleAtFixedRate(task, 0, SNAPSHOT_DELAY, TimeUnit.SECONDS);
    }

    logger.info("Galicaster Service activated");
  }

  protected void deactivate(ComponentContext cc) {
    periodicSnapshots.shutdownNow();
    logger.info("Galicaster Service deactivated");
  }


  /**
   * Sets the capture agent state service
   * 
   * @param captureAgentStateService
   *          the captureAgentStateService to set
   */
  protected void setCaptureAgentStateService(CaptureAgentStateService captureAgentStateService) {
    this.captureAgentStateService = captureAgentStateService;
  }

  /**
   * Sets the workspace
   *
   * @param workspace
   *         an instance of the workspace
   */
  protected void setWorkspace(Workspace workspace) {
    this.workspace = workspace;
  }

  /**
   * @param securityService
   *          the securityService to set
   */
  public void setSecurityService(SecurityService securityService) {
    this.securityService = securityService;
  }


  /**
   * Check the specified agent is active and reachable
   * @param agentName the name of the agent to check
   * @throws IOException if a network error occurs
   * @throws NotFoundException if the agent is not registered
   */
  public boolean isActive(String agentName) {
    Agent agent = captureAgentStateService.getAgentState(agentName);
    if (agent != null) {
      InetAddress addr;
      try {
        addr = InetAddress.getByName(agent.getUrl());
        boolean isReachable = addr.isReachable(PING_TIMEOUT);
        boolean isInGoodState = true;
        //boolean isInGoodState = goodStates.contains(agent.getState());
        logger.error("Agent {} is {}reachable{}.",
                new String[] {
                agent.getName(),
                isReachable ? "" : "not ",
                        ""
                        //isReachable ? " and reporting state " + agent.getState() : ""
        });

        return isReachable && isInGoodState;
      } catch (UnknownHostException e) {
        // Host does not exist
      } catch (IOException e) {
        // A network error occurred, so the agent is unreachable too
      } 
    } 

    return false;
  }

  public File getSnapshot(String agentName) throws NotFoundException {

    Agent agent = captureAgentStateService.getAgentState(agentName);

    if (agent != null) {
      String fileName = agentName + GstreamerSnapshotTaker.IMAGE_EXTENSION;

      try {
        return workspace.get(workspace.getCollectionURI(COLLECTION_NAME, fileName));
      } catch (IllegalArgumentException e) {
        logger.warn("Couldn't create a correct URI with file name '{}' and collection '{}'.", fileName, COLLECTION_NAME);
        throw new NotFoundException("Couldn't create a correct URI with collection name '" + COLLECTION_NAME + 
                "' and file name '" + fileName + "'.", e);
      } catch (IOException e) {
        logger.warn("Error reading file '{}' from collection '{}'", fileName, COLLECTION_NAME);
        throw new NotFoundException("Error reading file '" + fileName +
                "' from collection '" + COLLECTION_NAME + "'.", e);
      }
    } else
      throw new NotFoundException("Agent " + agentName + " is not registered in the system");
  }


  private final class SnapshotWithDeadline implements Runnable {

    /** Service to run the snapshot thread in a controlled environment, so that it can be interrupted if it takes too long */
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    Agent agent;
    String vncPassword;
    File destDir;
    long deadLine;
    TimeUnit timeUnits;

    public SnapshotWithDeadline(final Agent agent, final File destDir,
            final String vncPassword, final long deadLine, final TimeUnit timeUnits) {
      this.agent = agent;
      this.destDir = destDir;
      this.vncPassword = vncPassword;
      this.deadLine = deadLine;
      this.timeUnits = timeUnits;
    }

    @Override
    public void run() {
      if (agent != null) {

        String fileName = agent.getName() + GstreamerSnapshotTaker.IMAGE_EXTENSION;

        GstreamerSnapshotTaker cst = null;
        boolean deleteFileFromWorkspace = true;

        try {

          try {
            cst = new GstreamerSnapshotTaker(agent, destDir, vncPassword);
          } catch (IOException ioe) {
            throw new RuntimeException("Failed to initialize the snapshot taker", ioe);
          }

          File newFile = executor.submit(cst).get(deadLine, timeUnits);

          FileInputStream fis = null;
          try {
            if(newFile.isFile()) {
              fis = new FileInputStream(newFile);
              workspace.putInCollection(COLLECTION_NAME, fileName, fis);
              deleteFileFromWorkspace = false;
            } else {
              logger.error("'{}' is not a proper file.", newFile.getAbsolutePath());
              throw new RuntimeException(newFile.getAbsolutePath() + " is not a proper file.");
            }
          } finally {
            IOUtils.closeQuietly(fis);
            if (!newFile.delete())
              logger.warn("Couldn't delete temporary file: {}", newFile);
          }
        } catch (InterruptedException ie) {
          throw new RuntimeException("Unexpected interruption while waiting for job to finish", ie);
        } catch (ExecutionException ee) {
          throw new RuntimeException("Retrieving desktop snapshot from capture agent \"" + agent.getName() + "\" failed");
        } catch (TimeoutException toe) {
          throw new RuntimeException("Retrieving desktop snapshot from capture agent \"" + agent.getName() + "\" took too long");
        } catch (IOException ioe) {
          throw new RuntimeException("Failed to write new file to workspace", ioe);
        } catch (IllegalArgumentException iae) {
          throw new RuntimeException("Failed to create a URI for the workspace file", iae);
        } finally {
          cst.interrupt(); 
          if (deleteFileFromWorkspace) {
            try {
              workspace.deleteFromCollection(COLLECTION_NAME, fileName);
            } catch (NotFoundException e) {
              logger.warn("Couldn't delete snapshot: '{}' does not exist in the workspace.", fileName);
            } catch (IOException e) {
              logger.warn("Got IOException when deleting '{}' from workspace: {}", fileName, e.getMessage());
            }
          }
        }
      } 
    } 
  }

}
