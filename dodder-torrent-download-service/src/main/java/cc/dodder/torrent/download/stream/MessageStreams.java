package cc.dodder.torrent.download.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MessageStreams {

	@Input("download-message-in")
	MessageChannel downloadMessageInput();

	@Output("torrent-message-out")
	MessageChannel torrentMessageOutput();
}
