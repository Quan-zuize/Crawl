package hieu.dev.chapter9_webCrawler.entity;

import lombok.Data;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("postcode")
public class MBCEntity {
    @Id
    private String code;
    private String name;
    private List<MBCEntity> postCodes;
    private Integer index;

    public MBCEntity(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MBCEntity build(WebElement webElement) {
        String code = webElement.findElement(By.cssSelector("span:nth-child(1)")).getText();
        String name = webElement.findElement(By.cssSelector("span:nth-child(2)")).getText();
        return new MBCEntity(code, name);
    }
}
