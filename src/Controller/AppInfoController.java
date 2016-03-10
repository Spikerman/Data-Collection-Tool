package Controller;

import BasicData.AppData;
import Utils.Toolkit;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenhao on 2/7/16.
 */
public class AppInfoController {

    private static final String ITUNES_SEARCH_API =
            "http://itunes.apple.com/cn/lookup?id=%s";
    public int appSize = 100;
    public int threadsSize = 20;
    //补足appDataList的完整信息
    private List<AppData> appDataList = new ArrayList<>();
    private List appIdList = new ArrayList<>();
    private int retryTimes = 20;

    private List<String> errorIdList = Collections.synchronizedList(new LinkedList<>());
    private List<AppData> appInfoList = Collections.synchronizedList(new LinkedList<>());

    private Logger logger = LoggerFactory.getLogger(getClass());


    public AppInfoController() {

    }

    public List<AppData> getAppInfoList() {
        return appInfoList;
    }

    public List<String> getErrorIdList() {
        return errorIdList;
    }

    public List<AppData> addAppDataInfo(List<AppData> entryList, JSONObject jsonObject) {
        List<AppData> list = new LinkedList<>();
        try {
            int resultCount = (int) jsonObject.get("resultCount");
            int jsonObjectIndex = 0;

            for (int i = 0; i < entryList.size(); i++) {
                AppData appData = entryList.get(i);
                String trackId;

                if (jsonObjectIndex < resultCount)
                    trackId = jsonObject.getJSONArray("results").getJSONObject(jsonObjectIndex).get("trackId").toString();
                else {
                    System.out.println("x appDataList appSize: " + list.size());
                    return list;
                }
                while (!appData.getId().equals(trackId)) {
                    errorIdList.add(appData.getId());
                    System.out.println("error Id: " + appData.getId());
                    logger.info("error app Id: " + appData.getId());
                    i++;

                    if (i >= entryList.size()) {
                        System.out.println("x appDataList appSize: " + list.size());
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
            e.printStackTrace();
        }

        System.out.println("search app amount: " + list.size());
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

    public void setAppSize(int appSize) {
        this.appSize = appSize;
    }


    private List<List> getSubAppDataList() {
        List<List> subAppDataList;
        if (appDataList.size() == 0) {
            System.out.println("get nothing from crawler, function return");
            logger.info("get nothing from crawler, function return");
            return null;
        } else {
            logger.info("total app amount" + appDataList.size());
            subAppDataList = Toolkit.splitArray(appDataList, appSize);
            return subAppDataList;
        }
    }

    public List<AppData> fetchAppDetailInfo(List<AppData> dataList) {

        System.out.println("start fetch info");

        JSONObject jsonObject = getJSON(dataList);
        List<AppData> resultAppDataList = new LinkedList<>();

        if (jsonObject != null) {
            dataList = addAppDataInfo(dataList, jsonObject);
            resultAppDataList.addAll(dataList);
        } else {
            System.out.println("jsonObject=null, fetch error");
            logger.info("jsonObject=null, fetch error");
            return null;
        }
        return resultAppDataList;
    }

    //acquire all app info according to app id, and return the app data list,
    //return null if network error
    public List<AppData> fetchAppDetailInfo() {
        List<AppData> resultAppDataList = new LinkedList<>();
        List<List> subAppDataList;
        if (appDataList.size() == 0) {
            System.out.println("get nothing from crawler, function return");
            logger.info("get nothing from crawler, function return");
            return null;
        } else {
            subAppDataList = Toolkit.splitArray(appDataList, appSize);
        }
        System.out.println("start fetch info");

        for (int i = 0; i < subAppDataList.size(); i++) {
            List<AppData> dataList = subAppDataList.get(i);
            JSONObject jsonObject = getJSON(dataList);

            if (jsonObject != null) {
                dataList = addAppDataInfo(dataList, jsonObject);
                resultAppDataList.addAll(dataList);
            } else {
                System.out.println("jsonObject=null, fetch error");
                logger.info("jsonObject=null, fetch error");
                return null;
            }
        }

        System.out.println("fetch result amount: " + resultAppDataList.size());
        return resultAppDataList;
    }

    public JSONObject getJSON(List<AppData> appDataList) {

        JSONObject jsonObject = null;
        boolean success = false;
        InputStream inputStream = null;

        int i = 0;
        while (i < retryTimes) {
            try {

                String idListString = idListStringFormation(appDataList);
                URL url = new URL(String.format(ITUNES_SEARCH_API, idListString));
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                //set time out to avoid thread rigid
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);

                inputStream = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
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
                    System.out.println("json object count: " + resultCount);
                }

                success = true;
                break;

            } catch (IOException e) {
                System.out.println("network error " + "retry " + (i + 1) + " times");
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        //release the resource
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            i++;
        }

        if (success) {
            return jsonObject;
        } else {
            System.out.println("connect fail!");
            logger.info("connect fail!");
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

    public void startFetch() {
        //sub app data list includes list
        List<List> appDataListList = getSubAppDataList();

        //split the list of appDataListList again
        List<List<List>> tempList = Toolkit.splitArray(appDataListList, threadsSize);
        System.out.println("threadGroupSize: " + tempList.size());
        logger.info("threadGroupSize: " + tempList.size());

        int threadArrayNum = 1;
        for (List<List> subAppDataListList : tempList) {
            Thread[] threads = new Thread[subAppDataListList.size()];
            for (int i = 0; i < subAppDataListList.size(); i++) {
                List<AppData> list = subAppDataListList.get(i);
                Runnable runnable = new fetchRunnable(list);
                threads[i] = new Thread(runnable);
                threads[i].start();
                System.out.println("threads group " + threadArrayNum + ": " + "Thread " + (i + 1) + " start");
            }
            try {
                for (int i = 0; i < threads.length; i++) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("thread group " + threadArrayNum + " complete!");
            threadArrayNum++;
        }
        logger.info("result app size: " + appInfoList.size());
    }

    class fetchRunnable implements Runnable {
        List<AppData> appDataList = new LinkedList<>();

        public fetchRunnable(List<AppData> subAppDataList) {
            this.appDataList = subAppDataList;
        }

        public void run() {
            List temp = fetchAppDetailInfo(appDataList);
            if (temp != null) {
                appInfoList.addAll(temp);
            } else {
                System.out.println("fetch error");
                logger.info("fetch error");
                return;
            }
            System.out.println(appInfoList.size());
        }
    }

}
