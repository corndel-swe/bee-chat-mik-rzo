import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    private static final Set<User> connectedClients = Collections.newSetFromMap(new ConcurrentHashMap<>());

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
                User user = new User(connectContext.sessionId(), connectContext);
                connectedClients.add(user);
                System.out.println("Connected: " + connectContext.sessionId());
            });

            ws.onMessage((messageContext) -> {
                System.out.println("Message: " + messageContext.sessionId());
                Message message = objectMapper.readValue(messageContext.message(), Message.class);
                for (User client : connectedClients) {
                    if (message.getRecipientID().isEmpty()) {
                        if (!client.getId().equals(messageContext.sessionId())) {
                            client.receiveMessage(message);
                        }
                    }
                    if (client.getId().equals(message.getRecipientID())) {
                        client.receiveMessage(message);
                    }
                }
            });

            ws.onClose((closeContext) -> {
                connectedClients.removeIf(client -> client.getId().equals(closeContext.sessionId()));
                System.out.println("Closed: " + closeContext.sessionId());
            });

            ws.onError((errorContext) -> System.out.println("Error: " + errorContext.sessionId()));

        });

        app.start(5001);
    }

}