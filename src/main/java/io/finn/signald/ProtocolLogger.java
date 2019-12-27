package io.finn.signald;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.whispersystems.libsignal.logging.SignalProtocolLogger;

class ProtocolLogger implements SignalProtocolLogger {
  private static final Logger logger = LogManager.getLogger("signal-protocol");

  public void log(int priority, String tag, String message) {
    logger.debug("[" + tag + "] " + message);
  }
}