package hieu.dev.chapter9_webCrawler.model;

import lombok.Data;
import org.jsoup.nodes.Element;

@Data
public class PostCode {
    private String code;
    private String name;

    public static PostCode from(Element h4Element) {
        PostCode postCode = new PostCode();
        postCode.setCode(h4Element.select("span:eq(0)").text());
        postCode.setName(h4Element.select("span:eq(1)").text());
        return postCode;
    }
}
