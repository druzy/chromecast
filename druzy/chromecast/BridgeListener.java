package druzy.chromecast;

import java.util.EventListener;

public interface BridgeListener extends EventListener {

	public void newMessage(BridgeEvent event);
}
