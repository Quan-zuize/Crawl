package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.LongStream;

@Service
public class RoutineCrawler extends BaseSeleniumCrawler {
    public void crawl() throws IOException {
        ChromeDriver chromeDriver = new ChromeDriver();
        chromeDriver.manage().timeouts().implicitlyWait(Duration.of(1, ChronoUnit.SECONDS));
        chromeDriver.get("https://routine.vn/maplist");

        String lastPageHref = chromeDriver.findElement(By.cssSelector(".pages-item-last-pages > a[href]")).getAttribute("href");
        long lastPage = Long.parseLong(UriComponentsBuilder.fromHttpUrl(lastPageHref).build().getQueryParams().toSingleValueMap().get("p"));

        LongStream.rangeClosed(1, lastPage).forEach(page -> {
            String url = UriComponentsBuilder.fromHttpUrl("https://routine.vn/maplist").queryParam("p", page).toUriString();
            chromeDriver.get(url);
            chromeDriver.findElements(By.cssSelector("tr.tr-location"))
                    .forEach(store -> {
                        String name = store.findElement(By.cssSelector("td.store-name .title")).getText();
                        String address = store.findElement(By.cssSelector("div.address")).getText();
                        String googleLink = store.findElement(By.cssSelector(".locationLink[href]")).getAttribute("href");
                        saveDataWithHref(address, name, googleLink, "routine_places");
                    });
        });

    }
}
