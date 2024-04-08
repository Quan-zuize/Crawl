package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ParisGateAuxCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        Document document = Jsoup.connect("http://parisgateaux.vn/danh-sach-nha-hang-paris-gateaux/").get();
        document.select(".entry-content > .panel-layout > div > .panel-grid-cell")
                .not(".panel-grid-cell-empty")
                .forEach(store -> {
                    String name = store.selectFirst("h3").text();
                    String address = store.selectFirst(".panel-layout p").text().trim();
                    address = address.split(":")[1].replace("Số điện thoại", "").replace(".", "").trim();
                    String googleLink = store.selectFirst(".panel-layout iframe[src]").attr("src");
                    saveDataWithHref(address, name, googleLink, "paris_gate_aux_places");
                });
    }
}
