package hieu.dev.chapter9_webCrawler.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankEntity {
    @Id
    private String id;
    @Field
    private String name;
    @Field
    private String address;
    @Field
    private String type;
    @Field
    private Double lat;
    @Field
    private Double lon;

    public static BankEntity from(BaseEntity baseEntity){
        BankEntity bankEntity = new BankEntity();
        BeanUtils.copyProperties(baseEntity, bankEntity, "google");
        return bankEntity;
    }
}
