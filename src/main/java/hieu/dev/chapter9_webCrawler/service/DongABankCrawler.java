package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.entity.BankEntity;
import hieu.dev.chapter9_webCrawler.selenium.BaseSeleniumCrawler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.stream.Collectors;

import static hieu.dev.chapter9_webCrawler.Utils.normalize;

@Service
@Slf4j
public class DongABankCrawler {
    public static void main(String[] args) throws IOException {
//    @Autowired
//    private MongoTemplate mongoTemplate;

    //public void crawlData() throws IOException {
        String data = Files.readString(Paths.get("src/main/java/hieu/dev/chapter9_webCrawler/service/DongABankData.txt"));
        data = data.split("\"display\",\"ATM;")[1];
        data = data.split("]")[0];

        List<BankEntity> dongabanks = new LinkedList<>();
        String[] processData = data.split(",\"Chi Nhánh\",\"Chi Nhánh;");
        //Process entity with type is 'atm'
        String[] data_atm = processData[0].split(",\"ATM Thế hệ mới\",\"ATM Thế hệ mới;");
        List<String> atms = new ArrayList<>(List.of(data_atm[0].split("\",\"ATM;")));
        for (String atm : atms) {
            String[] infors = atm.split("\",\"");
            String[] attrs = infors[0].split(";\\s*");

            BankEntity entity = new BankEntity();
            entity.setType("atm");
            entity.setName(normalize(attrs[0].trim()));
            if (entity.getName().contains("lưu động")) continue;
            int addressIdx = attrs.length < 4 ? 2 : attrs.length - 1;
            String address = normalize(attrs[addressIdx].trim());
            if (attrs.length > addressIdx + 1) {
                address = address.concat(", ").concat(String.join(", ", java.util.Arrays.copyOfRange(attrs, addressIdx + 1, attrs.length)));
            }
            entity.setAddress(address);

            boolean nextIslongitude = true;
            for (String attr : infors) {
                try {
                    double num = Double.parseDouble(attr);
                    try {
                        int check_num = Integer.parseInt(attr);
                        break;
                    } catch (NumberFormatException e) {
                        //Do nothing
                    }
                    if (nextIslongitude) {
                        entity.setLon(num);
                        nextIslongitude = false;
                    } else {
                        entity.setLat(num);
                        break;
                    }
                } catch (NumberFormatException e) {
                    //Do nothing
                }
            }
            if(entity.getLon() == null || entity.getLat() == null){
                BaseSeleniumCrawler baseSeleniumCrawler = new BaseSeleniumCrawler();
                baseSeleniumCrawler.saveDataBank(entity.getAddress(), entity.getName(), "dongabanks");
            }else{
                dongabanks.add(entity);
            }
        }

        List<String> atms_newgen = new ArrayList<>(List.of(data_atm[1].split("\",\"ATM Thế hệ mới;")));
        for (String atm : atms_newgen) {
            if(atm.contains("CN") || atm.contains("PGD")){
                continue;
            }
            String[] infors = atm.split("\",\"");
            String[] attrs = infors[0].split(";\\s*");

            BankEntity entity = new BankEntity();
            entity.setType("atm");
            entity.setName(normalize(attrs[0].trim()));
            entity.setAddress(attrs.length > 3 ? normalize(attrs[3].trim()) : normalize(attrs[2].trim()));

            boolean nextIslongitude = true;
            for (String attr : infors) {
                try {
                    double num = Double.parseDouble(attr);
                    try {
                        int check_num = Integer.parseInt(attr);
                        break;
                    } catch (NumberFormatException e) {
                        //Do nothing
                    }
                    if (nextIslongitude) {
                        entity.setLon(num);
                        nextIslongitude = false;
                    } else {
                        entity.setLat(num);
                        break;
                    }
                } catch (NumberFormatException e) {
                    //Do nothing
                }
            }
            if(entity.getLon() == null || entity.getLat() == null){
                BaseSeleniumCrawler baseSeleniumCrawler = new BaseSeleniumCrawler();
                baseSeleniumCrawler.saveDataBank(entity.getAddress(), entity.getName(), "dongabanks");
            }else{
                dongabanks.add(entity);
            }
        }

        processData = processData[1].split("\",\"Phòng Giao Dịch\",\"Phòng Giao Dịch;");
        //Process entity with type is 'branch'
        List<String> branches = new ArrayList<>(List.of(processData[0].split("\",\"Chi Nhánh;")));
        for (String branch : branches) {
            String[] infors = branch.split("\",\"");
            String[] attrs = infors[0].split(";\\s*");

            BankEntity entity = new BankEntity();
            entity.setType("branch");
            entity.setName(normalize(attrs[0].trim()));
            entity.setAddress(normalize(attrs[attrs.length-1]).trim());
            Optional<BankEntity> existData = dongabanks.stream().filter(d -> d.getAddress().equals(entity.getAddress())).findFirst();
            if(existData.isPresent()){
                BankEntity updateBank = existData.get();
                updateBank.setType("branch");
                updateBank.setName(entity.getName());
                continue;
            }

            boolean nextIslongitude = true;
            for (String attr : infors) {
                try {
                    double num = Double.parseDouble(attr);
                    try {
                        int check_num = Integer.parseInt(attr);
                        break;
                    } catch (NumberFormatException e) {
                        //Do nothing
                    }
                    if (nextIslongitude) {
                        entity.setLon(num);
                        nextIslongitude = false;
                    } else {
                        entity.setLat(num);
                        break;
                    }
                } catch (NumberFormatException e) {
                    //Do nothing
                }
            }

            if(entity.getLon() == null || entity.getLat() == null){
                BaseSeleniumCrawler baseSeleniumCrawler = new BaseSeleniumCrawler();
                baseSeleniumCrawler.saveDataBank(entity.getAddress(), entity.getName(), "dongabanks");
            }else{
                dongabanks.add(entity);
            }
        }

        processData = processData[1].split(",\"POS\",\"POS;");
        //Process entity with type is 'office'
        List<String> offices = new ArrayList<>(List.of(processData[0].split("\",\"Phòng Giao Dịch;")));
        for (String office : offices) {
            String[] infors = office.split("\",\"");
            String[] attrs = infors[0].split(";\\s*");

            BankEntity entity = new BankEntity();
            entity.setType("office");
            entity.setName(normalize(attrs[0].trim()));
            entity.setAddress(normalize(attrs[attrs.length-1]).trim());
            Optional<BankEntity> existData = dongabanks.stream().filter(d -> d.getAddress().equals(entity.getAddress())).findFirst();
            if(existData.isPresent()){
                BankEntity updateBank = existData.get();
                updateBank.setType("office");
                updateBank.setName(entity.getName());
                continue;
            }

            boolean nextIslongitude = true;
            for (String attr : infors) {
                try {
                    double num = Double.parseDouble(attr);
                    try {
                        int check_num = Integer.parseInt(attr);
                        break;
                    } catch (NumberFormatException e) {
                        //Do nothing
                    }
                    if (nextIslongitude) {
                        entity.setLon(num);
                        nextIslongitude = false;
                    } else {
                        entity.setLat(num);
                        break;
                    }
                } catch (NumberFormatException e) {
                    //Do nothing
                }
            }

            if(entity.getLon() == null || entity.getLat() == null){
                BaseSeleniumCrawler baseSeleniumCrawler = new BaseSeleniumCrawler();
                baseSeleniumCrawler.saveDataBank(entity.getAddress(), entity.getName(), "dongabanks");
            }else{
                dongabanks.add(entity);
            }
        }

        processData = processData[1].split("\",\"Hội sở\",\"Hội sở;");
        String[] infors = processData[1].split("\",\"");
        String[] attrs = infors[0].split(";\\s*");
        BankEntity entity = new BankEntity();
        entity.setType("office");
        entity.setName(normalize(attrs[0].trim()));
        entity.setAddress(normalize(attrs[attrs.length-1]).trim());
        Optional<BankEntity> existData = dongabanks.stream().filter(d -> d.getAddress().equals(entity.getAddress())).findFirst();
        if(existData.isPresent()){
            BankEntity updateBank = existData.get();
            updateBank.setType("office");
            updateBank.setName(entity.getName());
        }

//        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, BankEntity.class);
//        dongabanks.forEach(bulkOperations::insert);
//        bulkOperations.execute();
    }
}
