package com.insta.hms.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Provides a convinent and proper mechanism to capture the output from a Process and pipe the
 * response to a stream. The output is captured in a background because some native platforms only
 * provide limited buffer size for standard input and output streams, failure to promptly write the
 * input stream or read the output stream of the subprocess may cause the subprocess to block, and
 * even deadlock.
 *
 */
public class ProcessOutput extends Thread {

  /** The is. */
  InputStream is;

  /** The os. */
  OutputStream os;

  /**
   * Instantiates a new process output.
   *
   * @param is       the is
   * @param redirect the redirect
   */
  public ProcessOutput(InputStream is, OutputStream redirect) {
    this.is = is;
    this.os = redirect;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Thread#run()
   */
  @Override
  public void run() {
    try {
      PrintWriter pw = null;
      if (os != null) {
        pw = new PrintWriter(os);
      }

      InputStreamReader isr = new InputStreamReader(is);
      BufferedReader br = new BufferedReader(isr);
      String line = null;
      while ((line = br.readLine()) != null) {
        if (pw != null) {
          pw.println(line);
        }
      }
      if (pw != null) {
        pw.flush();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }
}
