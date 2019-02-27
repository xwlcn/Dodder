package cc.dodder.torrent.store.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.MessageChannel;

public interface MessageStreams {
	@Input("torrent-message-in")
	MessageChannel torrentMessageInput();
}
