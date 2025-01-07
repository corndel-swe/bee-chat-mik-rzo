import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("recipientId")
    private String recipientID;
    private String content;

    public String getRecipientID() {
        return recipientID;
    }

    public String getContent() {
        return content;
    }

    public void setRecipientID(String recipientID) {
        this.recipientID = recipientID;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
