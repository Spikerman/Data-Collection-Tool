package Controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by chenhao on 2/15/16.
 */

public class DbController {
    public static final String url = "jdbc:mysql://localhost/Data";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "GroupTie";
    public static final String password = "grouptie123456";

    public static final String insertReviewSql
            = "insert into Review (id,userId,appId,rate,version,date) values(?,?,?,?,?,?)";
    public static final String insertAppInfoSql
            = "insert into AppInfo (appId,rankType,ranking,rankFloatNum,currentVersion,currentVersionReleaseDate,averageUserRating,averageUserRatingForCurrentVersion,userRatingCount,userRatingCountForCurrentVersion) "
            + "values(?,?,?,?,?,?,?,?,?,?)";

    public static final String insertAuthorSql
            = "insert into Author (userId,appId,groupId,reviewId) values(?,?,?,?)";

    public static final String insertUnavailableAppSql
            = "insert into UnavailableApp (appId) values (?)";

    public static final String selectAppIdSql
            = "select distinct appId from AppInfo";

    public static final String selectCandidateCluster = "SELECT clusterId,appId FROM Data.CandidateCluster";

    public static final String selectUnavailableAppSql = "SELECT * FROM Data.UnavailableApp";

    public Connection connection = null;
    public PreparedStatement insertReviewPst = null;
    public PreparedStatement insertAppInfoPst = null;
    public PreparedStatement insertAuthorPst = null;
    public PreparedStatement insertUnavailableAppPst = null;
    public PreparedStatement selectAppIdPst = null;
    public PreparedStatement selectGroupAppPst = null;
    public PreparedStatement selectUnavailableAppPst = null;
    public PreparedStatement selectCandidateClusterPst = null;


    public DbController() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connect Success");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        String sql = "insert into UnavailableApp values('5555555')";
        DbController dbController = new DbController();
        try {
            PreparedStatement preparedStatement = dbController.connection.prepareStatement(sql);
            preparedStatement.executeUpdate();
            //dbController.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertAuthorPst(String sql) {
        try {
            insertAuthorPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertReviewPst(String sql) {
        try {
            insertReviewPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertAppInfoPst(String sql) {
        try {
            insertAppInfoPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInsertUnavailableAppSqlPst(String sql) {
        try {
            insertUnavailableAppPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSelectAppIdPst(String sql) {
        try {
            selectAppIdPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSelectCandidateClusterSqlPst(String sql) {
        try {
            selectCandidateClusterPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSelectUnavailableAppPst(String sql) {
        try {
            selectUnavailableAppPst = connection.prepareStatement(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void close() {
        try {
            this.connection.close();
            this.insertReviewPst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
