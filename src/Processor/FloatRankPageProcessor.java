package Processor;

import BasicData.AppData;
import Controller.AppInfoController;
import Controller.DbController;
import Downloader.DataDownloader;
import Pipeline.FloatUpRankPipeline;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//get the app name and detail information from the aso100.com

public class FloatRankPageProcessor implements PageProcessor {

    public static final String FLOW_UP_FREE_URL = "http://aso100.com/rank/float/float/up";
    public static final String FLOW_DOWN_PAID_URL = "http://aso100.com/rank/float/float/down/brand/paid";
    private final String FLOW_DOWN_FREE_URL = "http://aso100.com/rank/float/float/down";
    private final String FLOW_UP_PAID_URL = "http://aso100.com/rank/float/float/up/brand/paid";
    private final String FLOW_UP_PAID_GAME_URL = "http://aso100.com/rank/float/float/up/genre/6014/brand/paid";
    private final String FLOW_DOWN_PAID_GAME_URL = "http://aso100.com/rank/float/float/down/brand/paid/genre/6014";

    private final String FLOW_UP_FREE_GAME_URL = "http://aso100.com/rank/float/float/up/genre/6014";
    private final String FLOW_DOWN_FREE_GAME_URL = "http://aso100.com/rank/float/float/down/brand/free/genre/6014";


    private Site site = Site.me().setCycleRetryTimes(5).setSleepTime(5000).setTimeOut(200000);
    private int size = 200;
    private List<String> urls = new ArrayList<>();

    private Pattern appIdPattern = Pattern.compile("appid/\\d+");

    private boolean isFirstPage = true;

    public FloatRankPageProcessor() {
        urls.add(FLOW_DOWN_FREE_URL);
        urls.add(FLOW_UP_PAID_URL);
        //urls.add(FLOW_DOWN_PAID_URL);
        //urls.add(FLOW_UP_PAID_GAME_URL);
        //urls.add(FLOW_DOWN_PAID_GAME_URL);
        //urls.add(FLOW_UP_FREE_GAME_URL);
        //urls.add(FLOW_DOWN_FREE_GAME_URL);

        System.out.println("Processor.FloatRankPageProcessor Start!");

    }

    public static void main(String args[]) {
        FloatRankPageProcessor floatRankPageProcessor = new FloatRankPageProcessor();
        AppInfoController appInfoController = new AppInfoController();
        DbController dbController = new DbController();

        Spider.create(floatRankPageProcessor)
                .addUrl(FloatRankPageProcessor.FLOW_DOWN_PAID_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(new DataDownloader())
                .run();

        dbController.setInsertAppInfoPst(DbController.insertAppInfoSql);
        appInfoController.startFetch();
        List<AppData> dataList = appInfoController.getAppInfoList();

        if (dataList != null) {
            for (AppData appData : dataList) {
                int i = 1;
                System.out.println(i + appData.ranking + "  " + appData.rankFloatNum + "  " + appData.rankType + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + "  " + appData.getUserRatingCount() + " " + appData.getScrapeTime());
                try {
                    insertAppInfo(appData, dbController);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                i++;
            }
        } else {
            System.out.println("fetch error, system end");
            return;
        }

        List<String> errorIdList = new LinkedList<>();
        errorIdList.addAll(appInfoController.getErrorIdList());

        dbController.setInsertUnavailableAppSqlPst(DbController.insertUnavailableAppSql);
        if (errorIdList.size() != 0) {
            for (String id : errorIdList) {
                try {
                    dbController.insertUnavailableAppPst.setString(1, id);
                    dbController.insertUnavailableAppPst.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("no unavailable app");
        }
    }

    public static void insertAppInfo(AppData appData, DbController dbController) throws SQLException {
        dbController.insertAppInfoPst.setString(1, appData.getId());
        dbController.insertAppInfoPst.setString(2, appData.getRankType());
        dbController.insertAppInfoPst.setInt(3, appData.ranking);
        dbController.insertAppInfoPst.setInt(4, appData.rankFloatNum);
        dbController.insertAppInfoPst.setString(5, appData.currentVersion);
        dbController.insertAppInfoPst.setString(6, appData.currentVersionReleaseDate);
        dbController.insertAppInfoPst.setDouble(7, appData.averageUserRating);
        dbController.insertAppInfoPst.setDouble(8, appData.averageUserRatingForCurrentVersion);
        dbController.insertAppInfoPst.setDouble(9, appData.userRatingCount);
        dbController.insertAppInfoPst.setDouble(10, appData.userRatingCountForCurrentVersion);
        dbController.insertAppInfoPst.setDate(11, new java.sql.Date(appData.getScrapeTime().getTime()));
        dbController.insertAppInfoPst.executeUpdate();

    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void process(Page page) {
        List<AppData> appDataList = new LinkedList<>();
        List<String> appIdList = new LinkedList<>();
        Document document = page.getHtml().getDocument();
        Elements thumbnails = document.getElementsByClass("thumbnail");

        // Elements contents = document.getElementsByClass("caption");
        for (int i = 0; i < thumbnails.size(); i++) {
            AppData appData = getMetaAppData(thumbnails.get(i), page);
            appDataList.add(appData);
            appIdList.add(appData.getId());
        }
        if (isFirstPage) {
            page.addTargetRequests(urls);
            isFirstPage = false;
        }

        page.putField("appDataList", appDataList);
        page.putField("appIdList", appIdList);
        page.putField("size", size);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public AppData getMetaAppData(Element thumbnail, Page page) {
        String href = thumbnail.child(0).attr("href");

        Matcher userIdMatcher = appIdPattern.matcher(href);
        userIdMatcher.find();
        String appId = userIdMatcher.group().replace("appid/", "");

        Element caption = thumbnail.getElementsByClass("caption").first();
        String name = caption.getElementsByTag("h5").first().text();
        Elements spans = caption.getElementsByTag("span");

        int rankNum;
        if (spans.get(0).text().contains("落榜"))
            rankNum = -1;
        else
            rankNum = Integer.parseInt(spans.get(0).text());

        String rankFloatString = spans.get(1).text();
        if (rankFloatString.contains("+"))
            rankFloatString = rankFloatString.replace("+", "");

        int rankFloatNum;
        if (spans.get(1).attr("class").equals("down"))
            rankFloatNum = 0 - Integer.valueOf(rankFloatString);
        else
            rankFloatNum = Integer.valueOf(rankFloatString);


        String type;
        String url = page.getRequest().getUrl();
        if (url.equals(FLOW_UP_FREE_URL)) {
            type = AppData.topFreeFlowUp;
        } else if (url.equals(FLOW_DOWN_FREE_URL)) {
            type = AppData.topFreeFlowDown;
        } else if (url.equals(FLOW_UP_PAID_URL)) {
            type = AppData.topPaidFlowUp;
        } else if (url.equals(FLOW_DOWN_PAID_URL)) {
            type = AppData.topPaidFlowDown;
        } else if (url.equals(FLOW_UP_PAID_GAME_URL)) {
            type = AppData.topPaidFlowUpGame;
        } else if (url.equals(FLOW_DOWN_PAID_GAME_URL)) {
            type = AppData.topPaidFlowDownGame;
        } else if (url.equals(FLOW_UP_FREE_GAME_URL)) {
            type = AppData.topFreeFlowUpGame;
        } else if (url.equals(FLOW_DOWN_FREE_GAME_URL)) {
            type = AppData.topFreeFlowDownGame;
        } else {
            type = "unknown";
            System.out.println("rank type unmatch");
        }

        System.out.println(appId + "  " + type + "  " + name + " " + rankNum + "  " + rankFloatNum);

        return new AppData(appId, rankNum, rankFloatNum, type);
    }
}
