package hieu.dev.chapter9_webCrawler.illness;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static hieu.dev.chapter9_webCrawler.service.FoodyCrawler.gson;

@Slf4j
@Service
public class HelloDoctorsCrawler {
    @Autowired
    MongoTemplate mongoTemplate;
    public void crawl() throws IOException {
        Document document = Jsoup.connect("https://hellodoctors.vn/tra-cuu-benh.html?fbclid=IwAR0rccvp5tNuvxWbFPseU0PqTIuN1UuRR35bpG8WHP6nuczRGD31fGj4J1Y").get();
        document.select(".listIllness a[href]").forEach(illness -> {
            String title = illness.attr("title");
            String href = illness.absUrl("href");
            try {
                Document detailDocument = Jsoup.connect(href).get();

                Elements elements = detailDocument.getElementsByClass("contentPost").first().children();

                String description = elements.remove(0).text();
                IllnessEntity entity = new IllnessEntity(title, description);

                String categoryDescription = "";
                StringBuilder data = new StringBuilder();
                for (Element element : elements) {
                    if (element.tagName().equals("ul") || !"text-align:justify".equals(element.attr("style"))) continue;
                    if (element.tagName().equals("h3") || element.tagName().equals("h2")) {
                        if (!data.isEmpty()) {
                            String code = CategoryEnum.getCodeByDesc(categoryDescription);
                            if(Strings.isNotEmpty(code)) {
                                entity.getData().put(code, data.toString());
                            }
                            data = new StringBuilder();
                        }
                        categoryDescription = element.text();
                    }

                    if (element.tagName().equals("p") && Strings.isNotEmpty(categoryDescription)) {
                        data.append(element.text()).append("\n");
                    }
                }

                System.out.println(gson.toJson(entity));
                mongoTemplate.save(entity);
            } catch (Exception e) {
                log.error("Error while crawl: {}", href);
            }
        });
    }
}
