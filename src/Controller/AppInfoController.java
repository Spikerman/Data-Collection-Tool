package Controller;

import BasicData.AppData;
import Utils.Toolkit;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenhao on 2/7/16.
 */
public class AppInfoController {

    private static final String ITUNES_SEARCH_API =
            "http://itunes.apple.com/cn/lookup?id=%s";

    private List<AppData> appDataList = new ArrayList<>();
    private List appIdList = new ArrayList<>();
    private int retryTimes = 5;

    public AppInfoController() {
    }

    public List<AppData> getAppDataList() {
        return appDataList;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public List getAppIdList() {
        return appIdList;
    }

    public void setAppIdList(List appIdList) {
        this.appIdList = appIdList;
    }


    public void appendAppIdList(List entryAppIdList) {
        appIdList.addAll(entryAppIdList);
    }

    public void appendAppDataList(List entryAppDataList) {
        appDataList.addAll(entryAppDataList);
    }

    //acquire all app info according to app id, and return the app data list,
    //return null if network error
    public List<AppData> fetchAppInfo() {

        List<List> subAppDataList = Toolkit.splitArray(appDataList, 100);

        for (List dataList : subAppDataList) {
            JSONObject jsonObject = getJSON(dataList);

            if (jsonObject != null) {
                addAppDataInfo(dataList, jsonObject);
            } else {
                System.out.println("jsonObject=null, fetch error");
                return null;
            }
        }

        //清空只含有id的appDataList元素,并重新填充信息完整的元素
        appDataList.clear();

        for (List subList : subAppDataList) {
            appDataList.addAll(subList);
        }

        return appDataList;
    }

    public JSONObject getJSON(List<AppData> appDataList) {

        JSONObject jsonObject = null;
        boolean success = false;

        int i = 0;

        while (i < retryTimes) {
            try {

                String idListString = idListStringFormation(appDataList);
                URL url = new URL(String.format(ITUNES_SEARCH_API, idListString));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer json = new StringBuffer(2048);

                String tmp;
                while ((tmp = reader.readLine()) != null) {
                    json.append(tmp).append("\n");
                }

                reader.close();
                jsonObject = new JSONObject(json.toString());

                if (0 == (int) jsonObject.get("resultCount")) {
                    jsonObject = null;
                } else {
                    System.out.println("json object result count: " + (int) jsonObject.get("resultCount"));
                }

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
        }
        return jsonObject;
    }

    public String idListStringFormation(List<AppData> entryList) {
        String idListString = "";
        for (AppData appData : entryList) {
            idListString += appData.getId() + ",";
        }
        return idListString;
    }

    //补足appDataList的完整信息
    public void addAppDataInfo(List<AppData> entryList, JSONObject jsonObject) {
        try {
            int index = 0;
            if (entryList.size() == (int) jsonObject.get("resultCount")) {
                for (AppData appData : entryList) {

                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("averageUserRating"))
                        appData.averageUserRating = 0;
                    else
                        appData.averageUserRating = (double) jsonObject.getJSONArray("results").getJSONObject(index).get("averageUserRating");

                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("averageUserRatingForCurrentVersion"))
                        appData.averageUserRatingForCurrentVersion = 0;
                    else
                        appData.averageUserRatingForCurrentVersion = (double) jsonObject.getJSONArray("results").getJSONObject(index).get("averageUserRatingForCurrentVersion");


                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("userRatingCount"))
                        appData.userRatingCount = 0;
                    else
                        appData.userRatingCount = (int) jsonObject.getJSONArray("results").getJSONObject(index).get("userRatingCount");

                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("userRatingCountForCurrentVersion"))
                        appData.userRatingCountForCurrentVersion = 0;
                    else
                        appData.userRatingCountForCurrentVersion = (int) jsonObject.getJSONArray("results").getJSONObject(index).get("userRatingCountForCurrentVersion");

                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("version"))
                        appData.currentVersion = "";
                    else
                        appData.currentVersion = jsonObject.getJSONArray("results").getJSONObject(index).get("version").toString();
//
                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("currentVersionReleaseDate"))
                        appData.currentVersionReleaseDate = "";
                    else
                        appData.currentVersionReleaseDate = jsonObject.getJSONArray("results").getJSONObject(index).get("currentVersionReleaseDate").toString();

                    index++;
                }
            } else {
                System.out.println("the size of app data list does not equal to the amount of return jsonObject!");
            }


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
