import us.codecraft.webmagic.Spider;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Created by chenhao on 2/18/16.
 */
public class DataCrawler {

    public static AppStorePaidRankProcessor appStorePaidRankProcessor = new AppStorePaidRankProcessor();
    public static FloatUpRankPageProcessor floatUpRankPageProcessor = new FloatUpRankPageProcessor();
    public static ReviewPageProcessor reviewPageProcessor;
    public static ProxyProcessor proxyProcessor = new ProxyProcessor();
    public static AppInfoController appInfoController = new AppInfoController();
    public static DbHelper dbHelper = new DbHelper();

    public static void main(String args[]) {

        Spider.create(appStorePaidRankProcessor)
                .addUrl(AppStorePaidRankProcessor.PAGE_URL)
                .addPipeline(new AppStorePaidRankPipeline(appInfoController))
                .thread(1)
                .run();

        Spider.create(floatUpRankPageProcessor)
                .addUrl(FloatUpRankPageProcessor.PAGE_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .run();


        dbHelper.setInsertAppInfoPst(DbHelper.insertAppInfoSql);

        List<AppData> dataList = appInfoController.fetchAppInfo();
        if (dataList != null) {
            for (AppData appData : dataList) {
                System.out.println(appData.ranking + "  " + appData.rankType + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + "  " + appData.getScrapeTime());
                try {
                    dbHelper.insertAppInfoPst.setString(1, appData.getId());
                    dbHelper.insertAppInfoPst.setString(2, appData.getRankType());
                    dbHelper.insertAppInfoPst.setInt(3, appData.ranking);
                    dbHelper.insertAppInfoPst.setDouble(4, appData.averageUserRating);
                    dbHelper.insertAppInfoPst.setDouble(5, appData.averageUserRatingForCurrentVersion);
                    dbHelper.insertAppInfoPst.setDouble(6, appData.userRatingCount);
                    dbHelper.insertAppInfoPst.setDouble(7, appData.userRatingCountForCurrentVersion);
                    dbHelper.insertAppInfoPst.setDate(8, new java.sql.Date(appData.getScrapeTime().getTime()));
                    dbHelper.insertAppInfoPst.executeUpdate();
                } catch (SQLException e) {
                    System.out.println("duplicate app, skip it");
                }
            }
        }


        proxyProcessor.setScrapePageCount(10);
        Spider.create(proxyProcessor)
                .addUrl(proxyProcessor.INITIAL_URL)
                .thread(1)
                .run();


        List appIdList = appInfoController.getAppIdList();
        appIdList = Toolkit.removeDuplicate(appIdList);
        for (Object id : appIdList) {
            reviewPageProcessor = new ReviewPageProcessor(id.toString());
            reviewPageProcessor.setProxyList(proxyProcessor.getProxyList());
            Spider.create(reviewPageProcessor)
                    .addUrl(ReviewPageProcessor.INITIAL_URL)
                    .thread(5)
                    .run();

            dbHelper.setInsertReviewPst(DbHelper.insertReviewSql);

            Set<Review> reviewSet = reviewPageProcessor.getReviewSet();
            for (Review review : reviewSet) {
                try {
                    dbHelper.insertReviewPst.setString(1, review.getId());
                    dbHelper.insertReviewPst.setString(2, review.getAppId());
                    dbHelper.insertReviewPst.setDouble(3, review.getRate());
                    dbHelper.insertReviewPst.setString(4, review.getVersion());
                    dbHelper.insertReviewPst.setDate(5, new java.sql.Date(review.getDate().getTime()));
                    dbHelper.insertReviewPst.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
