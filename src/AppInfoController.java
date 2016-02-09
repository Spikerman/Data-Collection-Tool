import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenhao on 2/7/16.
 */
public class AppInfoController {

    private static final String ITUNES_SEARCH_API =
            "http://itunes.apple.com/cn/lookup?id=%s";

    private List appIdList;

    public AppInfoController(List appIdList) {
        this.appIdList = appIdList;
    }

    public List getAppIdList() {
        return appIdList;
    }

    public void setAppIdList(List appIdList) {
        this.appIdList = appIdList;
    }


    public void fetchStart() {
        List<AppData> appDataList = new ArrayList<>();
        Iterator idIterator = appIdList.iterator();

        int x = 1;
        while (idIterator.hasNext()) {
            String id = idIterator.next().toString();
            AppData appData = new AppData(id);
            appData.ranking = x;
            appDataList.add(appData);
            x++;

        }

        List<List> subAppDataList = Toolkit.splitArray(appDataList, 100);

        for (List dataList : subAppDataList) {
            JSONObject jsonObject = getJSON(dataList);

            if (jsonObject != null) {
                addAppDataInfo(dataList, jsonObject);
            } else {
                System.out.println("jsonObject=null, fetch error");
            }
        }
    }

    public JSONObject getJSON(List<AppData> appDataList) {

        JSONObject jsonObject;

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
            } else
                System.out.println("json object result count: " + (int) jsonObject.get("resultCount"));
        } catch (Exception e) {
            System.out.println("network error");
            System.out.println(e.getMessage());
            jsonObject = null;
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

    public void addAppDataInfo(List<AppData> entryList, JSONObject jsonObject) {
        try {
            int index = 0;
            if (entryList.size() == (int) jsonObject.get("resultCount")) {
                for (AppData appData : entryList) {
                    appData.name = jsonObject.getJSONArray("results").getJSONObject(index).get("trackName").toString();

                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("averageUserRating"))
                        appData.averageUserRating = 0;
                    else
                        appData.averageUserRating = (double) jsonObject.getJSONArray("results").getJSONObject(index).get("averageUserRating");

                    if (jsonObject.getJSONArray("results").getJSONObject(index).isNull("userRatingCount"))
                        appData.userRatingCount = 0;
                    else
                        appData.userRatingCount = (int) jsonObject.getJSONArray("results").getJSONObject(index).get("userRatingCount");

                    System.out.println(appData.ranking + " " + appData.id + "  " + appData.name + "  " + appData.averageUserRating + "  " + appData.userRatingCount);
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
