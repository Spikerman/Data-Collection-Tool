import BasicData.AppData;
import BasicData.Review;
import Controller.AppInfoController;
import Controller.DbController;
import Downloader.DataDownloader;
import Pipeline.FloatUpRankPipeline;
import Processor.AppStoreRankProcessor;
import Processor.FloatRankPageProcessor;
import Processor.ProxyProcessor;
import Processor.ReviewPageProcessor;
import Utils.Toolkit;
import us.codecraft.webmagic.Spider;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Created by chenhao on 2/18/16.
 */
public class SuspiciousAppCollector {

    public static AppStoreRankProcessor appStoreRankProcessor = new AppStoreRankProcessor();
    public static FloatRankPageProcessor floatRankPageProcessor = new FloatRankPageProcessor();
    public static ReviewPageProcessor reviewPageProcessor;
    public static ProxyProcessor proxyProcessor = new ProxyProcessor();
    public static AppInfoController appInfoController = new AppInfoController();
    public static DbController dbController = new DbController();
    public static DataDownloader dataDownloader = new DataDownloader();
    public static List<String> failUrlList = new LinkedList<>();


    public static void main(String args[]) {
        Spider.create(floatRankPageProcessor)
                .addUrl(FloatRankPageProcessor.FLOW_UP_FREE_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(dataDownloader)
                .run();
        dbController.setInsertAppInfoPst(DbController.insertAppInfoSql);
        List<AppData> dataList = appInfoController.fetchAppDetailInfo();
        if (dataList != null) {
            for (AppData appData : dataList) {
                System.out.println(appData.ranking + "  " + appData.rankType + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + "  " + appData.getScrapeTime());
                try {
                    insertAppInfo(appData, dbController);
                } catch (SQLException e) {
                    System.out.println("duplicate app, skip it");
                }
            }
        }

        List appIdList = appInfoController.getAppIdList();
        appIdList = Toolkit.removeDuplicate(appIdList);
        appIdList = appIdList.subList(0, 3);

        for (Object id : appIdList) {
            reviewPageProcessor = new ReviewPageProcessor(id.toString());
            Spider.create(reviewPageProcessor)
                    .addUrl(ReviewPageProcessor.INITIAL_URL)
                    .thread(20)
                    .setDownloader(dataDownloader)
                    .run();

            dbController.setInsertReviewPst(DbController.insertReviewSql);
            dbController.setInsertAuthorPst(DbController.insertAuthorSql);

            Set<Review> reviewSet = reviewPageProcessor.getReviewSet();
            for (Review review : reviewSet) {
                try {
                    insertReview(review, dbController);
                } catch (SQLException e) {
                    System.out.println("duplicate one, skip it");
                }

                try {
                    insertAuthor(review, dbController);

                } catch (SQLException e) {
                    System.out.println("duplicate one, skip it");

                }
            }
        }
    }

    public static void insertReview(Review review, DbController dbController) throws SQLException {
        dbController.insertReviewPst.setString(1, review.getId());
        dbController.insertReviewPst.setString(2, review.getAuthorId());
        dbController.insertReviewPst.setString(3, review.getAppId());
        dbController.insertReviewPst.setDouble(4, review.getRate());
        dbController.insertReviewPst.setString(5, review.getVersion());
        dbController.insertReviewPst.setDate(6, new java.sql.Date(review.getDate().getTime()));
        dbController.insertReviewPst.executeUpdate();

    }

    public static void insertAuthor(Review review, DbController dbController) throws SQLException {
        dbController.insertAuthorPst.setString(1, review.getAuthorId());
        dbController.insertAuthorPst.setString(2, review.getAppId());
        dbController.insertAuthorPst.executeLargeUpdate();

    }

    public static void insertAppInfo(AppData appData, DbController dbController) throws SQLException {
        dbController.insertAppInfoPst.setString(1, appData.getId());
        dbController.insertAppInfoPst.setString(2, appData.getRankType());
        dbController.insertAppInfoPst.setInt(3, appData.ranking);
        dbController.insertAppInfoPst.setDouble(4, appData.averageUserRating);
        dbController.insertAppInfoPst.setDouble(5, appData.averageUserRatingForCurrentVersion);
        dbController.insertAppInfoPst.setDouble(6, appData.userRatingCount);
        dbController.insertAppInfoPst.setDouble(7, appData.userRatingCountForCurrentVersion);
        dbController.insertAppInfoPst.setDate(8, new java.sql.Date(appData.getScrapeTime().getTime()));
        dbController.insertAppInfoPst.executeUpdate();

    }

    //获取排名浮动大的应用的 Id
    public void fetchAllAppId() {
        Spider.create(floatRankPageProcessor)
                .addUrl(FloatRankPageProcessor.FLOW_UP_FREE_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(dataDownloader)
                .run();
    }

    //根据获取应用ID，获取这些应用在
    public void fetchAllAppInfo() {
        dbController.setInsertAppInfoPst(DbController.insertAppInfoSql);
        List<AppData> dataList = appInfoController.fetchAppDetailInfo();
        if (dataList != null) {
            for (AppData appData : dataList) {
                System.out.println(appData.ranking + "  " + appData.rankType + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + "  " + appData.getScrapeTime());
                try {
                    insertAppInfo(appData, dbController);
                } catch (SQLException e) {
                    System.out.println("duplicate app, skip it");
                }
            }
        }

    }

    public void fetchProxy() {
        proxyProcessor.setScrapePageCount(10);
        Spider.create(proxyProcessor)
                .addUrl(proxyProcessor.INITIAL_URL)
                .thread(3)
                .setDownloader(dataDownloader)
                .run();
    }

    public void fetchAppReviewAuthorInfo() {
        List appIdList = appInfoController.getAppIdList();
        appIdList = Toolkit.removeDuplicate(appIdList);

        for (Object id : appIdList) {
            reviewPageProcessor = new ReviewPageProcessor(id.toString());

            Spider.create(reviewPageProcessor)
                    .addUrl(ReviewPageProcessor.INITIAL_URL)
                    .thread(5)
                    .setDownloader(dataDownloader)
                    .run();

            dbController.setInsertReviewPst(DbController.insertReviewSql);
            dbController.setInsertAuthorPst(DbController.insertAuthorSql);

            Set<Review> reviewSet = reviewPageProcessor.getReviewSet();
            for (Review review : reviewSet) {
                try {
                    insertReview(review, dbController);
                } catch (SQLException e) {
                    System.out.println("duplicate one, skip it");
                }
                try {
                    insertAuthor(review, dbController);
                } catch (SQLException e) {
                    System.out.println("duplicate one, skip it");
                }

            }
        }

        dbController.close();
    }
}
