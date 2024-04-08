package hieu.dev.chapter7_shortUrl.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("url")
@Data
public class ShortUrlEntity {
    @Id
    private Long id;
    @Field
    private String shortenUrl;
    @Field
    private String url;
}
