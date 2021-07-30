package com.insta.hms.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * w3m provides a convinent transformation of a html document to a formatted plain text document.
 * This class provides a convinence wrapper to execute w3m as a Process.
 *
 */
public class W3mTransformer {

  /** The log. */
  private Logger log = LoggerFactory.getLogger(W3mTransformer.class);

  /** The columns. */
  private int columns;

  /** The extra lines. */
  private int extraLines;

  /** The separator. */
  private static byte[] SEPARATOR = System.getProperty("line.separator").getBytes();

  /**
   * Instantiates a new w 3 m transformer.
   *
   * @param columns    the columns
   * @param extraLines the extra lines
   */
  public W3mTransformer(int columns, int extraLines) {
    if (columns < 0) {
      throw new AssertionError("The column setting cannot be negative");
    }
    this.columns = columns;
    this.extraLines = extraLines;
  }

  /**
   * Converts the html contents to a equivalent raw formatted plain text.
   *
   * @param html the html
   * @return the byte[]
   */
  public byte[] toText(byte[] html) {
    byte[] rawformatted = null;
    Runtime rt = Runtime.getRuntime();
    try {
      Process proc = rt
          .exec("w3m -dump -T text/html -o display_charset=US-ASCII" + " -cols " + columns);

      ByteArrayOutputStream errorReader = new ByteArrayOutputStream();
      ProcessOutput errorGobbler = new ProcessOutput(proc.getErrorStream(), errorReader);

      ByteArrayOutputStream outputReader = new ByteArrayOutputStream();
      ProcessOutput outputGobbler = new ProcessOutput(proc.getInputStream(), outputReader);

      errorGobbler.start();
      outputGobbler.start();

      new ProcessInput(proc, html).start();

      int exitVal = proc.waitFor();
      log.debug("w3m exited with code: " + exitVal);

      if (exitVal != 0) {
        log.error("Failed to transform " + new String(html));
      }

      errorReader.flush();
      errorReader.close();

      log.debug("Adding extra lines: " + extraLines);
      for (int i = 0; i < extraLines; i++) {
        outputReader.write(SEPARATOR);
      }
      outputReader.flush();
      rawformatted = outputReader.toByteArray();
      outputReader.close();

    } catch (IOException ioException) {
      log.error("Unable to execute w3m", ioException);
    } catch (InterruptedException interruptedException) {
      log.error("Unable to execute w3m", interruptedException);
    }

    return rawformatted;
  }

  /**
   * The input to the Process is provide in a background thread because some native platforms only
   * provide limited buffer size for standard input and output streams, failure to promptly write
   * the input stream or read the output stream of the subprocess may cause the subprocess to block,
   * and even deadlock.
   *
   */
  class ProcessInput extends Thread {

    /** The process. */
    private Process process;

    /** The html. */
    private byte[] html;

    /**
     * Instantiates a new process input.
     *
     * @param process the process
     * @param html    the html
     */
    public ProcessInput(Process process, byte[] html) {
      this.process = process;
      this.html = html;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Thread#run()
     */
    public void run() {
      OutputStream stream = process.getOutputStream();
      BufferedOutputStream bufstream = new BufferedOutputStream(stream);
      try {
        bufstream.write(html);
        bufstream.flush();
      } catch (IOException ioException) {
        log.error("Error while passing input to w3m", ioException);
      } finally {
        try {
          bufstream.close();
        } catch (IOException ioException) {
          log.error("Error while closing stream", ioException);
        }
      }
    }
  }

}
