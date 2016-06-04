import BasicData.Review;
import Controller.DbController;
import Downloader.ReviewDataDownLoader;
import Processor.ReviewPageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by chenhao on 4/10/16.
 */
public class MainCrawler {
    public Map<Integer, Set<String>> appGroupMap = new HashMap<>();
    public ReviewDataDownLoader reviewDataDownloader = new ReviewDataDownLoader();
    private DbController dbController = new DbController();
    private Set<String> unavailableAppSet = new HashSet<>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public MainCrawler() {
        dbController.setSelectCandidateClusterSqlPst(DbController.selectCandidateCluster);
        dbController.setInsertAuthorPst(DbController.insertAuthorSql);
        dbController.setInsertReviewPst(DbController.insertReviewSql);
        dbController.setSelectUnavailableAppPst(DbController.selectUnavailableAppSql);
    }

    public static void main(String args[]) {
        MainCrawler mainCrawler = new MainCrawler();
        mainCrawler.getUnavailableApp();
        mainCrawler.buildCandidateClusterMap();
        mainCrawler.filterRemovedApp();

        //mainCrawler.startFetch();
    }


    public void buildCandidateClusterMap() {
        ResultSet resultSet = null;
        String appId;
        int clusterId;
        try {
            resultSet = dbController.selectCandidateClusterPst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    clusterId = resultSet.getInt("clusterId");
                    appId = resultSet.getString("appId");
                    insertToMap(clusterId, appId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //将存储在hashmap中的各组app group中,已经下架的APP去除
    public void filterRemovedApp() {
        for (Map.Entry<Integer, Set<String>> entry : appGroupMap.entrySet()) {
            int count;
            Set<String> appSet = entry.getValue();
            count = appSet.size();
            //appSet.removeAll(unavailableAppSet);
            Set<String> tempSet = new HashSet<>();
            for (String id : appSet) {
                if (unavailableAppSet.contains(id)) {
                    System.out.println("unavailable App id : " + id);
                    tempSet.add(id);
                }
            }
            appSet.removeAll(tempSet);
            count = count - appSet.size();
            System.out.println(entry.getKey() + " " + count);
        }
    }


    public void getUnavailableApp() {
        ResultSet resultSet = null;
        String appId;
        try {
            resultSet = dbController.selectUnavailableAppPst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    appId = resultSet.getString("appId");
                    unavailableAppSet.add(appId);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void insertToMap(Integer groupId, String appId) {
        if (appGroupMap.containsKey(groupId)) {
            appGroupMap.get(groupId).add(appId);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.add(appId);
            appGroupMap.put(groupId, newIdSet);
        }
    }

    private void fetchAppReviewData(int cluster, String appId) {
        ReviewPageProcessor reviewPageProcessor = new ReviewPageProcessor(appId);

        Spider.create(reviewPageProcessor)
                .addUrl(ReviewPageProcessor.INITIAL_URL)
                .thread(5)
                .setDownloader(reviewDataDownloader)
                .run();

        Set<Review> reviewSet = reviewPageProcessor.getReviewSet();

        for (Review review : reviewSet) {
            try {
                insertReview(review, dbController);
            } catch (SQLException e) {
                System.out.println("duplicate one, skip it");
            }
            try {
                insertAuthor(cluster, review, dbController);
            } catch (SQLException e) {
                System.out.println("duplicate one, skip it");
            }

        }
    }

    public void startFetch() {
        Iterator mapIterator = appGroupMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) mapIterator.next();
            int clusterId = (int) entry.getKey();
            Set<String> appIdSet = (Set) entry.getValue();
            int count = 0;
            Iterator setIterator = appIdSet.iterator();
            while (setIterator.hasNext()) {
                count++;
                String appId = (String) setIterator.next();
                fetchAppReviewData(clusterId, appId);
                logger.info("cluster ", clusterId, " has finished ", count, " apps and remains ", appIdSet.size() - count, " apps");
            }
            logger.info("group ", clusterId, " has finished");
        }
    }

    public void insertReview(Review review, DbController dbController) throws SQLException {
        dbController.insertReviewPst.setString(1, review.getId());
        dbController.insertReviewPst.setString(2, review.getAuthorId());
        dbController.insertReviewPst.setString(3, review.getAppId());
        dbController.insertReviewPst.setDouble(4, review.getRate());
        dbController.insertReviewPst.setString(5, review.getVersion());
        dbController.insertReviewPst.setDate(6, new java.sql.Date(review.getDate().getTime()));
        dbController.insertReviewPst.executeUpdate();
    }

    public void insertAuthor(int groupId, Review review, DbController dbController) throws SQLException {
        dbController.insertAuthorPst.setString(1, review.getAuthorId());
        dbController.insertAuthorPst.setString(2, review.getAppId());
        dbController.insertAuthorPst.setInt(3, groupId);
        dbController.insertAuthorPst.setString(4, review.getId());
        dbController.insertAuthorPst.executeUpdate();
    }
}
