package hieu.dev.chapter9_webCrawler.selenium;

import org.springframework.stereotype.Service;

import static hieu.dev.chapter9_webCrawler.Utils.doRetry;

@Service
public class DuLichHaNoiCrawler extends BaseSeleniumCrawler {
    public void crawl() {
        String googleDataLink ="https://www.google.com/maps/d/viewer?mid=1ib6aunfP2VNSb64T2bPptAKi6hod2q1w";
        saveGoogleDataLink(googleDataLink, address -> address, "du_lich_hn_places1");
    }
}
