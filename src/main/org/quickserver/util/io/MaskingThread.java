/*
 * This file is part of the QuickServer library 
 * Copyright (C) QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports, enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */

package org.quickserver.util.io;

import java.io.*;

/**
 * This class attempts to erase characters echoed to the console.
 * @since 1.4
 */
class MaskingThread extends Thread {
   private volatile boolean stop;
   //private char echochar = '*';

  /**
   * @param prompt The prompt displayed to the user
   */
   public MaskingThread(String prompt) {
      System.out.print(prompt);
   }

  /**
   * Begin masking until asked to stop.
   */
   public void run() {
      int priority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

      try {
         stop = true;
         while(stop) {
           System.out.print("\010 " /*+ echochar*/);
           try {
              // attempt masking at this rate
              Thread.currentThread().sleep(1);
           }catch (InterruptedException iex) {
              Thread.currentThread().interrupt();
              return;
           }
         }
      } finally { // restore the original priority
         Thread.currentThread().setPriority(priority);
      }
   }

  /**
   * Instruct the thread to stop masking.
   */
   public void stopMasking() {
      this.stop = false;
   }
}
