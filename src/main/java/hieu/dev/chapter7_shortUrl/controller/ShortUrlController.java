package hieu.dev.chapter7_shortUrl.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import hieu.dev.chapter7_shortUrl.algorithm.Base62;
import hieu.dev.chapter7_shortUrl.entity.ShortUrlEntity;
import hieu.dev.chapter7_shortUrl.service.TwitterIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

//@RestController
public class ShortUrlController {
    @Value("${sdi.redirect.url}")
    private String redirectUrl;
    private static final Cache<Object, Object> cache = Caffeine.newBuilder()
                .maximumSize(3000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

    @Autowired
    private TwitterIdGenerator idGenerator;
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * rate limiter
     * check duplicate
     */
    @PostMapping("api/v1/data/shorten")
    public String shortenUrl(@RequestBody String url, HttpServletRequest request) {
        ShortUrlEntity shortUrlEntity = new ShortUrlEntity();
        long id = idGenerator.generateId();
        String shortUrl = Base62.encode(id);
        shortUrlEntity.setId(id);
        shortUrlEntity.setShortenUrl(shortUrl);
        shortUrlEntity.setUrl(url);
        mongoTemplate.insert(shortUrlEntity);

        request.getRequestURL();
        return redirectUrl + request.getContextPath() + "/api/v1/" + shortUrl;
    }

    /**
     * 301 vs 302: permanently vs temporarily
     * 302 + cache server side
     * TODO: cache by redis string + expire time
     */
    @GetMapping("api/v1/{shortUrl}")
    public RedirectView redirect(@PathVariable String shortUrl) throws Exception {
        String url = (String) cache.getIfPresent(shortUrl);

        if(Strings.isEmpty(url)) {
            long id = Base62.decode(shortUrl);

            Query query = Query.query(Criteria.where("_id").is(id));
            ShortUrlEntity shortUrlEntity = mongoTemplate.findOne(query, ShortUrlEntity.class);
            if(Objects.isNull(shortUrlEntity)) {
                throw new Exception("Short url is not exist");
            }
            url = shortUrlEntity.getUrl();

            cache.put(shortUrl, url);
        }
//        request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.MOVED_PERMANENTLY);
        return new RedirectView(url);
    }
}
