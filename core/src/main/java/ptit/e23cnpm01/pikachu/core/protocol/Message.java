package ptit.e23cnpm01.pikachu.core.protocol;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.lang.reflect.Type;
import java.util.Map;
import ptit.e23cnpm01.pikachu.core.model.Cell;

/**
 * Một message JSON trên một dòng, trường type xác định loại (bọc JsonObject của Gson).
 *
 * <p>Ví dụ: {"type":"MOVE","roomId":12,"a":{"row":3,"col":5},"b":{"row":7,"col":2}}. Các trường khác type nằm
 * trong data; put() ghi một trường, các hàm get đọc lại (trường không có hoặc null thì trả null, hàm kiểu
 * nguyên thủy trả giá trị mặc định).
 *
 * <p>Phụ trách: Dev 2.
 */
public class Message {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private String type;
    private JsonObject data;

    public Message(String type) {
        this.type = type;
        this.data = new JsonObject();
    }

    public String getType() {
        return type;
    }

    /** Chuỗi JSON một dòng, type đứng đầu. Gson thoát ký tự xuống dòng trong chuỗi nên kết quả không có dòng mới. */
    public String toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", type);
        for (Map.Entry<String, JsonElement> e : data.entrySet()) {
            obj.add(e.getKey(), e.getValue());
        }
        return GSON.toJson(obj);
    }

    /** Dựng lại message từ một dòng JSON; dòng không phải đối tượng JSON có trường type thì ném IllegalArgumentException. */
    public static Message fromJson(String line) {
        JsonObject obj;
        try {
            obj = JsonParser.parseString(line).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalArgumentException("Invalid message: " + line, e);
        }
        JsonElement type = obj.remove("type");
        if (type == null || !type.isJsonPrimitive()) {
            throw new IllegalArgumentException("Message has no type field: " + line);
        }
        Message msg = new Message(type.getAsString());
        msg.data = obj;
        return msg;
    }

    /** Ghi trường key; value là chuỗi, số, boolean, Cell, đối tượng thực thể hoặc danh sách, được Gson chuyển sang JSON. */
    public Message put(String key, Object value) {
        data.add(key, GSON.toJsonTree(value));
        return this;
    }

    public boolean has(String key) {
        JsonElement e = data.get(key);
        return e != null && !e.isJsonNull();
    }

    public String getString(String key) {
        return has(key) ? data.get(key).getAsString() : null;
    }

    public int getInt(String key) {
        return has(key) ? data.get(key).getAsInt() : 0;
    }

    public boolean getBoolean(String key) {
        return has(key) && data.get(key).getAsBoolean();
    }

    public Cell getCell(String key) {
        return get(key, Cell.class);
    }

    /** Đọc trường key thành đối tượng kiểu clazz (ví dụ Player, Integer). */
    public <T> T get(String key, Class<T> clazz) {
        return has(key) ? GSON.fromJson(data.get(key), clazz) : null;
    }

    /** Đọc trường key thành kiểu có tham số, ví dụ new TypeToken&lt;ArrayList&lt;PlayerRanking&gt;&gt;() {}.getType(). */
    public <T> T get(String key, Type type) {
        return has(key) ? GSON.fromJson(data.get(key), type) : null;
    }
}
