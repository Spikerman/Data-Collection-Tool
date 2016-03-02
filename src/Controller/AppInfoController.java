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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenhao on 2/7/16.
 */
public class AppInfoController {

    private static final String ITUNES_SEARCH_API =
            "http://itunes.apple.com/cn/lookup?id=%s";
    public int size = 100;
    //补足appDataList的完整信息
    private List<AppData> appDataList = new ArrayList<>();
    private List appIdList = new ArrayList<>();
    private int retryTimes = 5;
    private List<String> errorIdList = new LinkedList<>();

    public AppInfoController() {
    }

    public List<AppData> addAppDataInfo(List<AppData> entryList, JSONObject jsonObject) {
        List<AppData> list = new LinkedList<>();

        try {
            int jsonObjectIndex = 0;

            for (int i = 0; i < entryList.size(); i++) {
                AppData appData = entryList.get(i);
                String trackId = jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("trackId").toString();

                while (!appData.getId().equals(trackId)) {
                    errorIdList.add(appData.getId());
                    System.out.println("error Id: "+appData.getId());

                    i++;

                    if (i >= size) {
                        System.out.println("appDataList size: " + list.size());
                        return list;
                    } else {
                        appData = entryList.get(i);
                    }
                }

                if (jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).isNull("averageUserRating"))
                    appData.averageUserRating = 0;
                else
                    appData.averageUserRating = (double) jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("averageUserRating");

                if (jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).isNull("averageUserRatingForCurrentVersion"))
                    appData.averageUserRatingForCurrentVersion = 0;
                else
                    appData.averageUserRatingForCurrentVersion = (double) jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("averageUserRatingForCurrentVersion");

                if (jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).isNull("userRatingCount"))
                    appData.userRatingCount = 0;
                else
                    appData.userRatingCount = (int) jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("userRatingCount");

                if (jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).isNull("userRatingCountForCurrentVersion"))
                    appData.userRatingCountForCurrentVersion = 0;
                else
                    appData.userRatingCountForCurrentVersion = (int) jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("userRatingCountForCurrentVersion");

                if (jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).isNull("version"))
                    appData.currentVersion = "";
                else
                    appData.currentVersion = jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("version").toString();

                if (jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).isNull("currentVersionReleaseDate"))
                    appData.currentVersionReleaseDate = "";
                else
                    appData.currentVersionReleaseDate = jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("currentVersionReleaseDate").toString();

                list.add(appData);
                jsonObjectIndex++;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("appDataList size: " + list.size());
        return list;
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

    public void setSize(int size) {
        this.size = size;
    }

    //acquire all app info according to app id, and return the app data list,
    //return null if network error
    public List<AppData> fetchAppDetailInfo() {
        List<AppData> resultAppDataList = new LinkedList<>();

        List<List> subAppDataList = Toolkit.splitArray(appDataList, size);

        System.out.println("start fetch info");

        for (int i = 0; i < subAppDataList.size(); i++) {
            List<AppData> dataList = subAppDataList.get(i);
            JSONObject jsonObject = getJSON(dataList);

            if (jsonObject != null) {
                dataList = addAppDataInfo(dataList, jsonObject);
                resultAppDataList.addAll(dataList);
            } else {
                System.out.println("jsonObject=null, fetch error");
                return null;
            }
        }

        System.out.println("total size: " + resultAppDataList.size());
        return resultAppDataList;
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
                StringBuffer json = new StringBuffer(4096);

                String tmp;
                while ((tmp = reader.readLine()) != null) {
                    json.append(tmp).append("\n");
                }

                reader.close();
                jsonObject = new JSONObject(json.toString());

                int resultCount = (int) jsonObject.get("resultCount");
                if (0 == resultCount) {
                    jsonObject = null;
                } else {
                    System.out.println("json object result count: " + resultCount);
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
            return jsonObject;
        } else {
            System.out.println("connect fail!");
            return null;
        }
    }

    public String idListStringFormation(List<AppData> entryList) {
        String idListString = "";
        int i = 0;
        for (AppData appData : entryList) {
            idListString += appData.getId() + ",";
            i++;
        }
        return idListString;
    }

}
