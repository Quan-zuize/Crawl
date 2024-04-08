package hieu.dev.chapter9_webCrawler.illness;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@Document("illness")
public class IllnessEntity {
    private String id;
    private String title;
    private String description;
    private Map<String, String> data;

    public IllnessEntity(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        data = new HashMap<>();
    }
    public IllnessEntity(String title, String description) {
        this.title = title;
        this.description = description;
        data = new HashMap<>();
    }
}
