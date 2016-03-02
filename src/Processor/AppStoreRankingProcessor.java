package Processor;

import BasicData.AppData;
import Controller.AppInfoController;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by chenhao on 3/1/16.
 */
public class AppStoreRankingProcessor {

    private static final String TOP_PAID_URL = "https://itunes.apple.com/cn/rss/toppaidapplications/limit=100/json";
    private static final String TOP_FREE_URL = "https://itunes.apple.com/cn/rss/topfreeapplications/limit=100/json";
    private static final String NEW_GAME_URL = "https://itunes.apple.com/cn/rss/newapplications/limit=100/genre=6014/json";
    private int retryTimes = 5;
    private List<AppData> appDataList = new LinkedList<>();
    private List<String> urlList = new LinkedList<>();

    public AppStoreRankingProcessor() {
        System.out.println("Itunes Search API START");

        urlList.add(TOP_PAID_URL);
        urlList.add(TOP_FREE_URL);
        urlList.add(NEW_GAME_URL);
    }

    public static void main(String args[]){
        AppInfoController appInfoController = new AppInfoController();
        AppStoreRankingProcessor appStoreRankingProcessor=new AppStoreRankingProcessor();
        appStoreRankingProcessor.fetchRankAppInfo();
        appInfoController.appendAppDataList(appStoreRankingProcessor.getAppDataList());
        appInfoController.fetchAppDetailInfo();
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

                System.out.println((i+1)+"  "+id+"  "+type);
                appDataList.add(new AppData(id, i + 1, type));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appDataList;
    }

    public void fetchRankAppInfo() {
        for (String url : urlList) {
            JSONObject jsonObject = getJSON(url);
            appDataList.addAll(getAppDataList(jsonObject, url));
        }
    }

    public List<AppData> getAppDataList() {
        return appDataList;
    }
}
