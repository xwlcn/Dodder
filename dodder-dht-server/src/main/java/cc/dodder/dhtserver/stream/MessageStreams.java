package cc.dodder.dhtserver.stream;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface MessageStreams {

	@Output(value = "download-message-out")
	MessageChannel downloadMessageOutput();
}
