import io.javalin.websocket.WsContext;

public class User {
    public String id;
    private final WsContext context;

    public User(String id, WsContext context) {
        this.id = id;
        this.context = context;
    }

    public void receiveMessage(Message message) {
        context.send(message);
    }

    public String getId() {
        return id;
    }
}
