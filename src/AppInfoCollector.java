import BasicData.AppData;
import Controller.AppInfoController;
import Controller.DbController;
import Downloader.DataDownloader;
import Pipeline.FloatUpRankPipeline;
import Processor.AppStoreRankingProcessor;
import Processor.FloatRankPageProcessor;
import us.codecraft.webmagic.Spider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenhao on 3/5/16.
 */
public class AppInfoCollector {

    public static void main(String args[]) {
        FloatRankPageProcessor floatRankPageProcessor = new FloatRankPageProcessor();
        AppInfoController appInfoController = new AppInfoController();
        AppStoreRankingProcessor appStoreRankingProcessor = new AppStoreRankingProcessor();

        //collect flow rank data information through aso100.com by crawler
        Spider.create(floatRankPageProcessor)
                .addUrl(FloatRankPageProcessor.FLOW_UP_FREE_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(new DataDownloader())
                .run();

        //collect top rank data information through iTunes api
        appInfoController.appendAppDataList(appStoreRankingProcessor.fetchRankAppInfo(), "iTunes rank ");

        //collect update information from old app data in Database
        appInfoController.appendAppDataList(getUpdateAppInfo(), "DB data");


        System.out.println("big list appSize: " + appInfoController.getAppDataList().size());

        //fetch app detail information through iTunes api
        appInfoController.startFetch();

        //get the result list and insert to mysql
        List<AppData> dataList = appInfoController.getAppInfoList();

        System.out.println("big result list appSize: " + dataList.size());

        insertIntoSql(dataList, appInfoController);

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

    public static List<AppData> getUpdateAppInfo() {
        DbController dbController = new DbController();
        dbController.setSelectAppIdPst(DbController.selectAppIdSql);
        List<AppData> appDataList = new LinkedList<>();
        ResultSet resultSet = null;
        try {
            resultSet = dbController.selectAppIdPst.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (resultSet != null) {
            try {
                while (resultSet.next()) {
                    appDataList.add(new AppData(resultSet.getString("appId"), "update"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return appDataList;
    }
}
