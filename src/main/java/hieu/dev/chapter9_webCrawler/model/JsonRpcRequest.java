package hieu.dev.chapter9_webCrawler.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JsonRpcRequest {
    private String jsonrpc;
    private String method;
    private Object params;
    private Integer id;

    public JsonRpcRequest(String shopId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("shop_id", new JsonPrimitive(Integer.valueOf(shopId)));
        this.id = Integer.valueOf(shopId);
        this.jsonrpc = "2.0";
        this.method = "call";
        this.params = jsonObject;
    }
}
