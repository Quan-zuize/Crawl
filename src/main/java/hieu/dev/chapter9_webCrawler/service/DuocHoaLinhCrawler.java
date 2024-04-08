package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DuocHoaLinhCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        for (int page = 0; ; page++) {
            String url = String.format("https://duochoalinh.vn/dai-ly-phan-phoi/page/%d/", page);
            System.out.println(url);
            Document document = Jsoup.connect(url).get();
            if (document.select("div.list-dis > div.phanphoi").isEmpty()) return;
            document.select("div.list-dis > div.phanphoi")
                    .forEach(store -> {
                        String name = store.selectFirst(".name-dis").text();
                        String address = store.selectFirst(".ten-p").text();
                        if(address.contains("nhà")) return;

                        String googleLink = null;
                        if (!store.select("iframe[src]").isEmpty()) {
                            googleLink = store.selectFirst("iframe[src]").attr("src");
                        }
                        saveDataWithHref(address, name, googleLink, "duoc_hoa_link_places");
                    });
        }

    }
}
