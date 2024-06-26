package hieu.dev.chapter9_webCrawler.service;

import hieu.dev.chapter9_webCrawler.selenium.DuLichHaNoiCrawler;
import hieu.dev.chapter9_webCrawler.selenium.PhucLongCrawler;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class AARunCrawlerService {
    TheCoffeeHouseCrawler theCoffeeHouseCrawler;
    PhucLongCrawler phucLongCrawler;
    CongCoffeeCrawler congCoffeeCrawler;
    TocotocoCrawler tocotocoCrawler;
    DingteaCrawler dingteaCrawler;
    BobapopCrawler bobapopCrawler;
    GongChaCrawler gongChaCrawler;
    TheAlleyCrawler theAlleyCrawler;
    TigerSugarCrawler tigerSugarCrawler;
    PheLaCrawler pheLaCrawler;
    GeminiCrawler geminiCrawler;
    GutaCoffeeCrawler gutaCoffeeCrawler;
    KatinatCrawler katinatCrawler;
    GPBankCrawler gpBankCrawler;
    BacABankCrawler bacABankCrawler;
    PVComBankCrawler pvComBankCrawler;
    OceanBankCrawler oceanBankCrawler;
    OfficeSaiGonCrawler officeSaiGonCrawler;
    OfficeHaNoiCrawler officeHaNoiCrawler;
    DuLichHaNoiCrawler duLichHaNoiCrawler;
    HDBankCrawler hdBankCrawler;
    PublicBankCrawler publicBankCrawler;
    BibomartCrawler bibomartCrawler;
    TuticareCrawler tuticareCrawler;
    KidPlazaCrawler kidPlazaCrawler;
    MeoiCrawler meoiCrawler;
    AvaKidsCrawler avaKidsCrawler;
    MyKingdomCrawler myKingdomCrawler;
    TinyStoreCrawler tinyStoreCrawler;
    Konni39Crawler konni39Crawler;
    TgddCrawler tgddCrawler;
    DienmayxanhCrawler dienmayxanhCrawler;
    BachHoaXanhCrawler bachHoaXanhCrawler;
    TopZoneCrawler topZoneCrawler;
    FptShopCrawler fptShopCrawler;
    CellPhonesCrawler cellPhonesCrawler;
    ViettelStoreCrawl viettelStoreCrawl;
    DiDongVietCrawler diDongVietCrawler;
    HoangHaMobileCrawler hoangHaMobileCrawler;
    ClickByCrawler clickByCrawler;
    PhongVuCrawler phongVuCrawler;
    HacomCrawler hacomCrawler;
    MediaMartCrawler mediaMartCrawler;
    HcCrawler hcCrawler;
    PicoCrawler picoCrawler;
    CoopMartCrawler coopMartCrawler;
    MMVietNamCrawler mmVietNamCrawler;
    AeonMaxValuCrawler aeonMaxValuCrawler;
    AeonCrawler aeonCrawler;
    WinMartCrawler winMartCrawler;
    CircleKCrawler circleKCrawler;
    BsSmartVinaCrawler bsSmartVinaCrawler;
    MinistopCrawler ministopCrawler;
    SevenElementCrawler sevenElementCrawler;
    Gs25Crawler gs25Crawler;
    HomeFarmCrawler homeFarmCrawler;
    SoiBienCrawler soiBienCrawler;
    CpFoodsCrawler cpFoodsCrawler;
    BacTomCrawler bacTomCrawler;
    GoFoodCrawler goFoodCrawler;
    THMilkCrawler thMilkCrawler;
    VinamilkCrawler vinamilkCrawler;
    NutiFoodCrawler nutiFoodCrawler;
    MocChauCrawler mocChauCrawler;
    NEMCrawler nemCrawler;
    EliseCrawler eliseCrawler;
    GuardianCrawler guardianCrawler;
    CoopSmileCrawler coopSmileCrawler;
    IvyModaCrawler ivyModaCrawler;
    OwenCrawler owenCrawler;
    AnPhuocCrawler anPhuocCrawler;
    ChicLandCrawler chicLandCrawler;
    SevenamCrawler sevenamCrawler;
    HMCrawler hmCrawler;
    May10Crawler may10Crawler;
    NoveltyCrawler noveltyCrawler;
    YodyCrawler yodyCrawler;
    TheBluesCrawler theBluesCrawler;
    HoangPhucCrawler hoangPhucCrawler;
    EvadeevaCrawler evadeevaCrawler;
    Fashion5sCrawler fashion5sCrawler;
    JunoCrawler junoCrawler;
    GumacCrawler gumacCrawler;
    CanifaCrawler canifaCrawler;
    AdamStoreCrawler adamStoreCrawler;
    YameCrawler yameCrawler;
    BiluxuryCrawler biluxuryCrawler;
    RoutineCrawler routineCrawler;
    PhanNguyenCrawler phanNguyenCrawler;
    FmCrawler fmCrawler;
    AristinoCrawler aristinoCrawler;
    TokyoLifeCrawler tokyoLifeCrawler;
    UniqloCrawler uniqloCrawler;
    ToranoCrawler toranoCrawler;
    BitisCrawler bitisCrawler;
    BitasCrawler bitasCrawler;
    AcfcCrawler acfcCrawler;
    KrikCrawl krikCrawl;
    RabityCrawler rabityCrawler;
    EllyCrawler ellyCrawler;
    Shine30Crawler shine30Crawler;
    SeoulspaCrawler seoulspaCrawler;
    ThamMyDivaCrawler thamMyDivaCrawler;
    VuaNemCrawler vuaNemCrawler;
    PnjCrawler pnjCrawler;
    SjcCrawler sjcCrawler;
    DojiCrawler dojiCrawler;
    BtmcCrawler btmcCrawler;
    HuyThanhJewelryCrawler huyThanhJewelryCrawler;
    KimNgocThuyCrawler kimNgocThuyCrawler;
    AnhKhoaBakeryCrawler anhKhoaBakeryCrawler;
    OrigatoCrawler origatoCrawler;
    NguyenSonCrawler nguyenSonCrawler;
    FreshGardenCrawler freshGardenCrawler;
    GivralBakeryCrawler givralBakeryCrawler;
    SavoureBakeryCrawler savoureBakeryCrawler;
    ParisGateAuxCrawler parisGateAuxCrawler;
    BamiKingCrawler bamiKingCrawler;
    BreadtalkCrawler breadtalkCrawler;
    TuhuBreadCrawler tuhuBreadCrawler;
    ConcungCrawler concungCrawler;
    BigCCrawler bigCCrawler;
    GoVietnamCrawler goVietnamCrawler;
    VietTienCrawler vietTienCrawler;
    DuocHoaLinhCrawler duocHoaLinhCrawler;
    DuocThaiMinhCrawler duocThaiMinhCrawler;
    MilanoCoffeeCrawler milanoCoffeeCrawler;
    JtexpressCrawler jtexpressCrawler;
    GhnCrawler ghnCrawler;
    NinjavanCrawler ninjavanCrawler;
    BatdongsanCrawler batdongsanCrawler;
    RSquareCrawler rSquareCrawler;
    HoanKiem360Crawler hoanKiem360Crawler;
    NhatNhatCrawler nhatNhatCrawler;
    IvivuCrawler ivivuCrawler;
    MytourCrawler mytourCrawler;
    ChuDuCrawler chuDuCrawler;
    VntripCrawler vntripCrawler;
    GotadiCrawler gotadiCrawler;
    VntripcomCrawler vntripcomCrawler;
    AgribankCrawler agribankCrawler;
    DongABankCrawler dongABankCrawler;
    PgBankCrawler pgBankCrawler;
    MiuteaCrawlerService miuteaCrawlerService;

    @PostConstruct
    void init() throws IOException {
//        miuteaCrawlerService.crawl();
//        pgBankCrawler.crawl();
//        dongABankCrawler.crawlData();
//        agribankCrawler.crawl();
//        theCoffeeHouseCrawler.crawlTheCoffeeHouse();
//        phucLongCrawler.crawlPhucLong();
//        congCoffeeCrawler.crawlCongCoffee();
//        tocotocoCrawler.crawTocotoco();
        dingteaCrawler.crawDingTea();
//        bobapopCrawler.crawl();
//        gongChaCrawler.crawlGongCha();
//        theAlleyCrawler.crawlTheAlley();
//        tigerSugarCrawler.crawlTigerSugar();
//        pheLaCrawler.crawlPheLa();
//        geminiCrawler.crawlGemini();
//        gutaCoffeeCrawler.crawlGutaCoffee();
//        katinatCrawler.crawlKatinat();
//        gpBankCrawler.crawlGPBank();
//        pvComBankCrawler.crawl();
//        oceanBankCrawler.crawl();
//        officeSaiGonCrawler.crawl();
//        officeHaNoiCrawler.crawl();
//        duLichHaNoiCrawler.crawl();
//        hdBankCrawler.crawl();
//        publicBankCrawler.crawl();
//        bibomartCrawler.crawl();
//        tuticareCrawler.crawl();
//        kidPlazaCrawler.crawl();
//        meoiCrawler.crawl();
//        avaKidsCrawler.crawl();
//        myKingdomCrawler.crawl();
//        tinyStoreCrawler.crawl();
//        konni39Crawler.crawl();
//        tgddCrawler.crawl();
//        dienmayxanhCrawler.crawl();
//        bachHoaXanhCrawler.crawl();
//        topZoneCrawler.crawl();
//        fptShopCrawler.crawl();
//        cellPhonesCrawler.crawl();
//        viettelStoreCrawl.crawl();
//        diDongVietCrawler.crawl();
//        hoangHaMobileCrawler.crawl();
//        clickByCrawler.crawl();
//        phongVuCrawler.crawl();
//        hacomCrawler.crawl();
//        mediaMartCrawler.crawl();
//        hcCrawler.crawl();
//        picoCrawler.crawl();
//        coopMartCrawler.crawl();
//        mmVietNamCrawler.crawl();
//        aeonMaxValuCrawler.crawl();
//        aeonCrawler.crawl();
//        winMartCrawler.crawl();
//        circleKCrawler.crawl();
//        bsSmartVinaCrawler.crawl();
//        ministopCrawler.crawl();
//        sevenElementCrawler.crawl();
//        gs25Crawler.crawl();
//        homeFarmCrawler.crawl();
//        soiBienCrawler.crawl();
//        cpFoodsCrawler.crawl();
//        bacTomCrawler.crawl();
//        goFoodCrawler.crawl();
//        thMilkCrawler.crawl();
//        vinamilkCrawler.crawl();
//        nutiFoodCrawler.crawl();
//        mocChauCrawler.crawl();
//        nemCrawler.crawl();
//        eliseCrawler.crawl();
//        guardianCrawler.crawl();
//        coopSmileCrawler.crawl();
//        ivyModaCrawler.crawl();
//        owenCrawler.crawl();
//        anPhuocCrawler.crawl();
//        chicLandCrawler.crawl();
//        sevenamCrawler.crawl();
//        hmCrawler.crawl();
//        may10Crawler.crawl();
//        noveltyCrawler.crawl();
//        yodyCrawler.crawl();
//        theBluesCrawler.crawl();
//        hoangPhucCrawler.crawl();
//        evadeevaCrawler.crawl();
//        fashion5sCrawler.crawl();
//        junoCrawler.crawl();
//        gumacCrawler.crawl();
//        canifaCrawler.crawl();
//        adamStoreCrawler.crawl();
//        yameCrawler.crawl();
//        biluxuryCrawler.crawl();
//        routineCrawler.crawl();
//        phanNguyenCrawler.crawl();
//        fmCrawler.crawl();
//        aristinoCrawler.crawl();
//        tokyoLifeCrawler.crawl();
//        uniqloCrawler.crawl();
//        toranoCrawler.crawl();
//        bitisCrawler.crawl();
//        bitasCrawler.crawl();
//        acfcCrawler.crawl();
//        krikCrawl.crawl();
//        rabityCrawler.crawl();
//        ellyCrawler.crawl();
//        shine30Crawler.crawl();
//        seoulspaCrawler.crawl();
//        thamMyDivaCrawler.crawl();
//        vuaNemCrawler.crawl();
//        pnjCrawler.crawl();
//        sjcCrawler.crawl();
//        dojiCrawler.crawl();
//        btmcCrawler.crawl();
//        huyThanhJewelryCrawler.crawl();
//        kimNgocThuyCrawler.crawl();
//        anhKhoaBakeryCrawler.crawl();
//        origatoCrawler.crawl();
//        nguyenSonCrawler.crawl();
//        freshGardenCrawler.crawl();
//        givralBakeryCrawler.crawl();
//        savoureBakeryCrawler.crawl();
//        parisGateAuxCrawler.crawl();
//        bamiKingCrawler.crawl();
//        breadtalkCrawler.crawlData();
//        tuhuBreadCrawler.crawl();
//        concungCrawler.crawl();
//        bigCCrawler.crawl();
//        goVietnamCrawler.crawl();
//        vietTienCrawler.crawl();
//        duocHoaLinhCrawler.crawl();
//        duocThaiMinhCrawler.crawl();
//        milanoCoffeeCrawler.crawl();
//        jtexpressCrawler.crawl();
//        ghnCrawler.crawl();
//        ninjavanCrawler.crawl();
//        batdongsanCrawler.crawl();
//        rSquareCrawler.crawl();
//        hoanKiem360Crawler.crawl();
//        nhatNhatCrawler.crawl();
//        ivivuCrawler.crawl();
//        mytourCrawler.crawl();
//        chuDuCrawler.crawl();
//        vntripCrawler.crawl();
//        gotadiCrawler.crawl();
//        vntripcomCrawler.crawl();
    }
}
