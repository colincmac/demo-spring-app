package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.azure.messaging.webpubsub.*;
import com.azure.messaging.webpubsub.models.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class Application {

	static WebPubSubServiceClient pubSubClient;

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(Application.class, args);
		System.out.println("Starting App");

		pubSubClient = new WebPubSubServiceClientBuilder()
				.connectionString(
						"Endpoint=https://<my-pubsub>.webpubsub.azure.com;AccessKey=<primary-access-key>;Version=1.0;")
				.hub("testHub")
				.buildClient();

		WebPubSubClientAccessToken token = pubSubClient.getClientAccessToken(new GetClientAccessTokenOptions());
		System.out.println(String.format("Token URL: %s", token.getUrl()));

		WebSocketClient webSocketClient = InitializeWebSocketSubscriber();

		System.out.println("Subscriber Initialized");

		sendMessage("Hello World!");

		System.out.println("Message Sent");

	}

	public static WebSocketClient InitializeWebSocketSubscriber() throws URISyntaxException {
		WebPubSubClientAccessToken token = pubSubClient.getClientAccessToken(new GetClientAccessTokenOptions());

		WebSocketClient client = new WebSocketClient(new URI(token.getUrl())) {
			@Override
			public void onMessage(String message) {
				System.out.println(String.format("Message received: %s", message));
			}

			@Override
			public void onCloseInitiated(int code, String reason) {
				System.out.println(String.format("Connection Closing: %s, %s", code, reason));
			}

			@Override
			public void onClose(int arg0, String arg1, boolean arg2) {
				System.out.println(String.format("Connection Closed: %s, %s, %s", arg0, arg1, arg2));
			}

			@Override
			public void onClosing(int code, String reason, boolean remote) {
				// TODO Auto-generated method stub
				System.out.println(String.format("Connection Closing: %s", reason));
			}

			@Override
			public void onError(Exception arg0) {
				System.out.println(String.format("Connection Error: %s", arg0));
			}

			@Override
			public void onOpen(ServerHandshake arg0) {
				System.out.println(String.format("Connection Open: %s", arg0.getHttpStatusMessage()));
			}
		};
		client.connect();
		return client;
	}

	private static void sendMessage(String message) {
		pubSubClient.sendToAll(message, WebPubSubContentType.TEXT_PLAIN);
	}

}
