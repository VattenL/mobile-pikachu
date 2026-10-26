package ptit.e23cnpm01.pikachu.core.protocol;

import com.google.gson.JsonObject;

/**
 * Một message JSON trên một dòng, trường type xác định loại (bọc JsonObject của Gson).
 *
 * <p>Phụ trách: Dev 2.
 */
public class Message {
    private String type;
    private JsonObject data;

    public Message(String type) {
        this.type = type;
        this.data = new JsonObject();
    }

    public String getType() {
        return type;
    }

    public String toJson() {
        throw new UnsupportedOperationException("TODO");
    }

    public static Message fromJson(String line) {
        throw new UnsupportedOperationException("TODO");
    }
}
