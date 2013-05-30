package org.galicaster.dashboard.snapshot;

import org.opencastproject.capture.admin.api.Agent;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;

/**
 * This class implements a simple "snapshot taker" based on a Gstramer pipeline that connects via VNC to capture the screen
 *
 */
public class GstreamerSnapshotTaker implements Callable<File> {

  private static final Logger logger = LoggerFactory.getLogger(GstreamerSnapshotTaker.class);
  private static final String EXEC_NAME = "gst-launch";
  public static final String DEFAULT_RFB_VERSION = "3.0";

  /** Default extension for the snapshots extracted from the agents*/
  public static final String IMAGE_EXTENSION = ".png"; 
 
  private File destFile;
  private Agent agent;
  private String password, rfbVersion;

  Process p = null;

  public static final String pipeStr = "%s " +
                  "rfbsrc host=\"%s\" view-only=true password=\"%s\" version=\"%s\" num-buffers=1 ! " +
                  "ffmpegcolorspace ! " +
                  "pngenc ! " +
                  "filesink location=%s";


  public GstreamerSnapshotTaker(Agent agent, File destDir) throws IOException {
    this(agent, destDir, null, null);
  }

  public GstreamerSnapshotTaker(Agent agent, File destDir, String password) throws IOException {
    this(agent, destDir, password, null);
  }

  public GstreamerSnapshotTaker(Agent agent, File destDir, String password, String rfbVersion) throws IOException {

    if (agent == null)
      throw new NullPointerException("The agent must not be null");

    this.destFile = new File(destDir, agent.getName() + IMAGE_EXTENSION);
    this.agent = agent;
    this.password = password == null ? "" : password;
    this.rfbVersion = rfbVersion == null ? DEFAULT_RFB_VERSION : rfbVersion;
  };


  @Override
  public File call() {

    // Marks the file to be deleted on exist, if an error occurred
    boolean deleteOnExit = true;
    
    ProcessBuilder pb = new ProcessBuilder(
            String.format(pipeStr,
                    EXEC_NAME,
                    agent.getUrl(),
                    password,
                    rfbVersion,
                    destFile.getAbsolutePath()).split(" "));

    Scanner s = null; 
    try {
      p = pb.start();

      if (p.waitFor() != 0) {
        s = new Scanner(p.getErrorStream()).useDelimiter("\\A");
        String error = s.hasNext() ? s.next() : "";

        error = String.format("The subprocess returned error code %s: %s", p.exitValue(), error);
        logger.error(error);
        throw new RuntimeException(error);
      }

      if (!destFile.isFile())
        throw new RuntimeException("The subprocess did not create a file");

      deleteOnExit = false;
      
      return destFile;

    } catch (IOException e) {
      logger.error("Error when initiating the helper subprocess: {}", e.getMessage());
      throw new RuntimeException("Error when initiating the helper subprocess.", e);
    } catch (InterruptedException e) {
      logger.error("The subprocess was unexpectedly interrupted: {}", e.getMessage());
      throw new RuntimeException("The subprocess was unexpectedly interrupted.", e);
    } finally {
      if (s != null)
        s.close();
      if (deleteOnExit)
        FileUtils.deleteQuietly(destFile);
    }

  }

  /**
   * Interrupts the pipeline
   */
  public void interrupt() {
    p.destroy();
  }


}
