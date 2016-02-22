import us.codecraft.webmagic.Spider;

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
                .addPipeline(new PaidRankPipeline(appInfoController))
                .thread(1)
                .run();

        Spider.create(floatUpRankPageProcessor)
                .addUrl(FloatUpRankPageProcessor.PAGE_URL)
                .addPipeline(new UpRankPipeline(appInfoController))
                .thread(1)
                .run();

        List appIdList = appInfoController.getAppIdList();
        appIdList = Toolkit.removeDuplicate(appIdList);

        System.out.println("-----------------------");
        System.out.println("all id list");

        int i = 1;
        for (Object id : appIdList) {
            System.out.println(i + " " + id.toString());
            i++;
        }

        System.out.println("-----------------------");
        System.out.println("all app info list");
        dbHelper.setInsertAppInfoPst(DbHelper.insertAppInfoSql);
        List<AppData> dataList = appInfoController.fetchAppInfo();
        if (dataList != null) {
            for (AppData appData : dataList) {
                System.out.println(appData.ranking + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion);
                try {
                    dbHelper.insertAppInfoPst.setString(1, appData.id);
                    dbHelper.insertAppInfoPst.setInt(2, appData.ranking);
                    dbHelper.insertAppInfoPst.setDouble(3, appData.averageUserRating);
                    dbHelper.insertAppInfoPst.setDouble(4, appData.averageUserRatingForCurrentVersion);
                    dbHelper.insertAppInfoPst.setDouble(5, appData.userRatingCount);
                    dbHelper.insertAppInfoPst.setDouble(6, appData.userRatingCountForCurrentVersion);
                    dbHelper.insertAppInfoPst.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        proxyProcessor.setScrapePageCount(10);
        Spider.create(proxyProcessor)
                .addUrl(proxyProcessor.INITIAL_URL)
                .thread(1)
                .run();


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
