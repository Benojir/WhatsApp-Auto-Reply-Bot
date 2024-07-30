package zo.ro.whatsappreplybot.models;

public class Message {
    private int id;
    private String sender;
    private String message;
    private String timestamp;
    private String reply;

    public Message(int id, String sender, String message, String timestamp, String reply) {
        this.id = id;
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
        this.reply = reply;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}

