import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by chenhao on 2/15/16.
 */
public class DbHelper {
    public static final String url = "jdbc:mysql://127.0.0.1/Data";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "root";

    public static final String insertReviewSql
            = "insert into Review (id,appId,rate,version,date) values(?,?,?,?,?)";
    public static final String insertAppInfoSql
            = "insert into AppInfo (id,ranking,averageUserRating,averageUserRatingForCurrentVersion,userRatingCount,userRatingCountForCurrentVersion) values(?,?,?,?,?,?)";

    public Connection connection = null;
    public PreparedStatement insertReviewPst = null;
    public PreparedStatement insertAppInfoPst=null;



    public DbHelper() {
        try {
            Class.forName(name);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String args[]) {
        String sql = "insert into Author values('123456','654321')";
        DbHelper dbHelper = new DbHelper();
        try {
            dbHelper.insertReviewPst.executeUpdate();
            dbHelper.close();
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
        try{
            insertAppInfoPst=connection.prepareStatement(sql);
        }catch(Exception e){
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
