import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final Set<WsContext> connectedClients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();

        Javalin app = Javalin.create(javalinConfig -> {
            // Modifying the WebSocketServletFactory to set the socket timeout to 120 seconds
            javalinConfig.jetty.modifyWebSocketServletFactory(jettyWebSocketServletFactory ->
                    jettyWebSocketServletFactory.setIdleTimeout(Duration.ofSeconds(120))
            );
        });

        app.ws("/", ws -> {

            ws.onConnect((connectContext) -> {
                connectedClients.add(connectContext);
                System.out.println("Connected: " + connectContext.sessionId());
            });

            ws.onMessage((messageContext) -> {
                System.out.println("Message: " + messageContext.sessionId());
                Message message = objectMapper.readValue(messageContext.message(), Message.class);
                if (message.getRecipientID().isEmpty()) {
                    for (WsContext client : connectedClients) {
                        if (!client.sessionId().equals(messageContext.sessionId())) {
                            client.send(new HashMap<>(Map.of("senderId", 0, "content", "Hello from the server!")));
                        }
                    }
                }
            });

            ws.onClose((closeContext) -> {
                connectedClients.remove(closeContext);
                System.out.println("Closed: " + closeContext.sessionId());
            });

            ws.onError((errorContext) -> {
                System.out.println("Error: " + errorContext.sessionId());
            });

        });

        app.start(5001);
    }

}