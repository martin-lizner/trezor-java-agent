package org.multibit.hd.hardware.core.fsm;

import org.multibit.hd.hardware.core.HardwareWalletClient;
import org.multibit.hd.hardware.core.events.MessageEvent;
import org.multibit.hd.hardware.core.messages.Features;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>State to provide the following to hardware wallet clients:</p>
 * <ul>
 * <li>State transitions based on low level message events</li>
 * </ul>
 * <p>The "connected" state is a transitional state occurring when the
 * underlying communication transport confirms connection. In USB terms the device
 * is claimed, in a socket the server has accepted.</p>
 *
 * <p>The next state is normally Initialised</p>
 *
 * @since 0.0.1
 *  
 */
public class ConnectedState extends AbstractHardwareWalletState {

  private static final Logger log = LoggerFactory.getLogger(ConnectedState.class);

  @Override
  public void await(HardwareWalletClient client, HardwareWalletContext context) {

    // Trigger a state transition via the response event
    client.initialize();

  }

  @Override
  protected void internalTransition(HardwareWalletClient client, HardwareWalletContext context, MessageEvent event) {

    switch (event.getMessageType()) {
      case FEATURES:
        context.setFeatures((Features) event.getMessage().get());
        context.resetToInitialised();
        break;
      default:
        log.info("Unexpected message event '{}'", event.getMessageType().name());
        context.resetToConnected();
    }

  }
}