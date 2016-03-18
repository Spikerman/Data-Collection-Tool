package Processor;

import BasicData.AppData;
import Controller.AppInfoController;
import Controller.DbController;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenhao on 3/1/16.
 */
public class AppStoreRankingProcessor {

    private static final String TOP_PAID_URL = "https://itunes.apple.com/cn/rss/toppaidapplications/limit=100/json";
    private static final String TOP_FREE_URL = "https://itunes.apple.com/cn/rss/topfreeapplications/limit=100/json";
    private static final String NEW_GAME_URL = "https://itunes.apple.com/cn/rss/newapplications/limit=100/genre=6014/json";
    private int retryTimes = 20;
    private List<AppData> appDataList = new LinkedList<>();
    private List<String> urlList = new LinkedList<>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AppStoreRankingProcessor() {
        System.out.println("Itunes Search API START");

        urlList.add(TOP_PAID_URL);
        urlList.add(TOP_FREE_URL);
        urlList.add(NEW_GAME_URL);
    }

    public static void insertIntoSql(List<AppData> dataList, AppInfoController appInfoController) {
        DbController dbController = new DbController();

        dbController.setInsertAppInfoPst(DbController.insertAppInfoSql);
        dbController.setInsertUnavailableAppSqlPst(DbController.insertUnavailableAppSql);

        if (dataList != null) {
            int i = 1;

            for (AppData appData : dataList) {
                System.out.println(i + "  " + appData.ranking + "  " + appData.rankFloatNum + "  " + appData.rankType + " " + appData.id + "  " + "  " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + " " + appData.getScrapeTime());
                try {
                    FloatRankPageProcessor.insertAppInfo(appData, dbController);
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

    public static void main(String args[]) {
        AppInfoController appInfoController = new AppInfoController();
        AppStoreRankingProcessor appStoreRankingProcessor = new AppStoreRankingProcessor();
        appInfoController.appendAppDataList(appStoreRankingProcessor.fetchRankAppInfo(), "iTunes rank ");
        appInfoController.startFetch();
        List<AppData> dataList = appInfoController.getAppInfoList();
        insertIntoSql(dataList, appInfoController);

    }

    private JSONObject getJSON(String urlString) {

        JSONObject jsonObject = null;
        boolean success = false;

        int i = 0;
        while (i < retryTimes) {
            try {

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer json = new StringBuffer(4096);

                String tmp;
                while ((tmp = reader.readLine()) != null) {
                    json.append(tmp).append("\n");
                }
                reader.close();
                jsonObject = new JSONObject(json.toString());

                success = true;
                break;

            } catch (IOException e) {
                System.out.println("network error" + "retry " + (i + 1) + " times");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            i++;
        }

        if (success) {
            System.out.println("connect success!");
        } else {
            System.out.println("connect fail!");
            logger.info("connect fail!");
        }
        return jsonObject;
    }

    private List<AppData> getAppDataList(JSONObject jsonObject, String urlString) {
        List<AppData> appDataList = new LinkedList<>();
        try {
            for (int i = 0; i < 100; i++) {
                String id = "null";
                if (!jsonObject.getJSONObject("feed").getJSONArray("entry").getJSONObject(i).getJSONObject("id").getJSONObject("attributes").isNull("im:id"))
                    id = jsonObject.getJSONObject("feed").getJSONArray("entry").getJSONObject(i).getJSONObject("id").getJSONObject("attributes").get("im:id").toString();

                String type;
                if (urlString.equals(TOP_FREE_URL))
                    type = AppData.topFree;
                else if (urlString.equals(TOP_PAID_URL))
                    type = AppData.topPaid;
                else
                    type = AppData.newGame;

                int rank;
                if (type.equals(AppData.newGame))
                    rank = 0;
                else
                    rank = i + 1;

                System.out.println(rank + "  " + id + "  " + type);
                appDataList.add(new AppData(id, rank, type));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appDataList;
    }

    public List<AppData> fetchRankAppInfo() {
        for (String url : urlList) {
            JSONObject jsonObject = getJSON(url);
            appDataList.addAll(getAppDataList(jsonObject, url));
        }
        return appDataList;
    }

    public List<AppData> getAppDataList() {
        return appDataList;
    }


}
