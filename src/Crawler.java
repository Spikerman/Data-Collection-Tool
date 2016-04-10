import Controller.DbController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by chenhao on 4/10/16.
 */
public class Crawler {
    public Map<Integer, Set<String>> appGroupMap = new HashMap<>();
    private DbController dbController = new DbController();

    public Crawler() {
        dbController.setSelectGroupAppSqlPst(DbController.selectGroupAppSql);
    }

    public static void main(String args[]) {
        Crawler crawler = new Crawler();
        crawler.buildAppGroupMap();
        System.out.println("end");
    }

    public void buildAppGroupMap() {
        ResultSet resultSet = null;
        String appId;
        int groupId;
        try {
            resultSet = dbController.selectGroupAppPst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    groupId = resultSet.getInt("groupId");
                    appId = resultSet.getString("appId");
                    insertToMap(groupId, appId);
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


}
