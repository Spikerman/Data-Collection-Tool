import BasicData.Review;
import Controller.DbController;
import Downloader.ReviewDataDownLoader;
import Processor.ReviewPageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Spider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by chenhao on 4/10/16.
 */

//为数据库中的应用获取各个应用下的所有评论信息
public class AppReviewCrawler {
    public Map<Integer, Set<String>> candidateClusterMap = new HashMap<>();
    public ReviewDataDownLoader reviewDataDownloader = new ReviewDataDownLoader();
    public Set<String> baseAppSet = new HashSet<>();//存取数据库中已有的APP记录
    private DbController dbController = new DbController();
    private Set<String> unavailableAppSet = new HashSet<>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AppReviewCrawler() {
        dbController.setSelectCandidateClusterSqlPst(DbController.selectCandidateCluster);
        dbController.setInsertAuthorPst(DbController.insertAuthorSql);
        dbController.setInsertReviewPst(DbController.insertReviewSql);
        dbController.setSelectUnavailableAppPst(DbController.selectUnavailableAppSql);

        //读取数据库中已有的APP记录
        loadReviewData();

        //对数据库中读取的记录按照组号构建 Map
        buildCandidateClusterMap();
    }

    public static void main(String args[]) {
        AppReviewCrawler appReviewCrawler = new AppReviewCrawler();
        appReviewCrawler.startFetch();
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

    public void loadReviewData() {
        String sql = "SELECT * FROM Data.Review";
        Statement statement;
        ResultSet rs;
        try {
            statement = dbController.connection.createStatement();
            rs = statement.executeQuery(sql);
            String appId;
            while (rs.next()) {
                appId = rs.getString("appId");
                baseAppSet.add(appId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //将存储在hashmap中的各组app group中,已经下架的APP去除
    public void filterRemovedApp() {
        for (Map.Entry<Integer, Set<String>> entry : candidateClusterMap.entrySet()) {
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
        if (candidateClusterMap.containsKey(groupId)) {
            candidateClusterMap.get(groupId).add(appId);
        } else {
            Set<String> newIdSet = new HashSet<>();
            newIdSet.add(appId);
            candidateClusterMap.put(groupId, newIdSet);
        }
    }

    public void fetchAppReviewData(int cluster, String appId) {
        if (!baseAppSet.contains(appId)) {
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
        } else {
            logger.info(appId + " already exist in database, skip it");
        }
    }

    public void startFetch() {
        Iterator mapIterator = candidateClusterMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry entry = (Map.Entry) mapIterator.next();
            int clusterId = (int) entry.getKey();
            Set<String> appIdSet = (Set) entry.getValue();
            Iterator setIterator = appIdSet.iterator();
            while (setIterator.hasNext()) {
                String appId = (String) setIterator.next();
                fetchAppReviewData(clusterId, appId);
            }
            logger.info("cluster ", clusterId, " has finished");
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
