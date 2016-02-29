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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//get the app name and detail information from the aso100.com

public class FloatUpRankPageProcessor implements PageProcessor {

    public static final String PAGE_URL = "http://aso100.com/index.php/rank/float?float=up";
    private Site site = Site.me().setCycleRetryTimes(5).setSleepTime(2000).setTimeOut(200000);
    private int size = 200;

    private Pattern appIdPattern = Pattern.compile("appid/\\d+");

    public FloatUpRankPageProcessor() {
        System.out.println("Processor.FloatUpRankPageProcessor Start!");
    }

    public static void main(String args[]) {
        FloatUpRankPageProcessor floatUpRankPageProcessor = new FloatUpRankPageProcessor();
        AppInfoController appInfoController = new AppInfoController();
        DbController dbController = new DbController();

        Spider.create(floatUpRankPageProcessor)
                .addUrl(FloatUpRankPageProcessor.PAGE_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(new DataDownloader())
                .run();

        dbController.setInsertAppInfoPst(DbController.insertAppInfoSql);
        List<AppData> dataList = appInfoController.fetchAppInfo();
        if (dataList != null) {
            for (AppData appData : dataList) {
                System.out.println(appData.ranking + "  " + "  " + appData.rankFloatNum + appData.rankType + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + "  " + appData.getUserRatingCount() + "" + appData.getScrapeTime());
                try {
                    insertAppInfo(appData, dbController);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
//        List<AppData> appDataList = appInfoController.getAppDataList();
//        int i = 1;
//        for (AppData x : appDataList) {
//            System.out.println(i++ + "  " + x.getId());
//        }

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
            AppData appData = getDetailInfo(thumbnails.get(i));
            appDataList.add(appData);
            appIdList.add(appData.getId());
        }

        page.putField("appDataList", appDataList);
        page.putField("appIdList", appIdList);
        page.putField("size", size);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public AppData getDetailInfo(Element thumbnail) {
        String href = thumbnail.child(0).attr("href");

        Matcher userIdMatcher = appIdPattern.matcher(href);
        userIdMatcher.find();
        String appId = userIdMatcher.group().replace("appid/", "");

        Element caption = thumbnail.getElementsByClass("caption").first();
        String name = caption.getElementsByTag("h5").first().text();
        Elements spans = caption.getElementsByTag("span");
        int rankNum = Integer.parseInt(spans.get(0).text());
        String rankFloatString = spans.get(1).text();
        if (rankFloatString.contains("+"))
            rankFloatString = rankFloatString.replace("+", "");
        int rankFloatNum = Integer.valueOf(rankFloatString);

        System.out.println(appId + "  " + name + " " + rankNum + "  " + rankFloatNum);

        return new AppData(appId, rankNum, rankFloatNum, AppData.topFlowUp);
    }
}
