package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BankEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler.preHandle;

@Service
@Slf4j
public class PgBankCrawler{
    BaseSeleniumCrawler baseSeleniumCrawler = new BaseSeleniumCrawler();

    public void crawl() throws IOException {
        List<BankEntity> pgbanks = new LinkedList<>();

        Connection connect = Jsoup.connect("https://thebank.vn/cong-cu/tim-chi-nhanh-ngan-hang/pg-bank-31.html");
        Document document = connect.get();
        Elements cityElements = document.select("div[id='template_data_company'] > a");
        for (Element element: cityElements){
            Connection child_connect = Jsoup.connect("https://thebank.vn/cong-cu/tim-chi-nhanh-ngan-hang/" + element.attr("href"));
            Document child_document = child_connect.get();
            Elements provinceElements = child_document.select("div[id='template_data_province_content'] > a");
            for(Element child_element: provinceElements){
                Connection grandchild_connect = Jsoup.connect("https://thebank.vn/cong-cu/tim-chi-nhanh-ngan-hang/" + child_element.attr("href"));
                Document grandchild_document = grandchild_connect.get();
                Elements bankElements = grandchild_document.select("tbody[class='content_branch roboto-light'] > tr");
                for(Element bank: bankElements) {
                    BankEntity entity = new BankEntity();
                    String name = bank.getElementsByClass("brad").get(0).text();
                    entity.setName(!name.contains("PG Bank") ? "Ngân hàng Pg ".concat(name) : name);
                    entity.setAddress(bank.getElementsByTag("td").get(2).text());
                    String googleSearch = preHandleGGBank(entity.getAddress(), entity.getName());
                    baseSeleniumCrawler.saveDataV2(entity.getAddress(), entity.getName(), googleSearch, "pgbanks");
                }
            }
        }
    }

    private String preHandleGGBank(String address, String name) {
        address = preHandle(address);
        return name.concat(", ").concat(address.split("(?i),\\s*Tỉnh")[0]);
    }
}
