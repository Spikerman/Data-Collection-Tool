import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;


/**
 * Created by chenhao on 2/5/16.
 */

public class ObjConstruction implements Pipeline {

    private static final String ITUNES_SEARCH_API=
            "http://itunes.apple.com/cn/lookup?id=%s";


    @Override
    public void process(ResultItems resultItems, Task task){

        List appNameList=resultItems.get("nameList");
        List appIdList=resultItems.get("idList");

        List<AppData> appDataList=new ArrayList<>();

        Iterator nameIterator=appNameList.iterator();
        Iterator idIterator=appIdList.iterator();

        int x=1;
        while(nameIterator.hasNext()&&idIterator.hasNext()){

            String name=nameIterator.next().toString();
            String id=idIterator.next().toString();

            //JSONObject jsonObject=getJSON(id);
            AppData appData=new AppData(name,id);

            appDataList.add(appData);

            x++;

        }

        JSONObject jsonObject=getJSON(appDataList);
        if(jsonObject!=null){
            addAppDataInfo(appDataList,jsonObject);
        }else{
            System.out.println("jsonObject=null, fetch error");
        }




    }

    public JSONObject getJSON(List<AppData>appDataList){

        JSONObject jsonObject;

        try {

            String idListString=idListStringFormation(appDataList);
            URL url = new URL(String.format(ITUNES_SEARCH_API, idListString));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer json=new StringBuffer(2048);

            String tmp;

            while((tmp=reader.readLine())!=null){
                json.append(tmp).append("\n");
            }

            reader.close();
            jsonObject=new JSONObject(json.toString());


            if(0==(int)jsonObject.get("resultCount"))
                jsonObject=null;
            System.out.println("json object result count: "+(int)jsonObject.get("resultCount"));


            //System.out.println(jsonObject);


        }catch (Exception e){
            System.out.println("network error");
            jsonObject=null;
        }
    return jsonObject;
    }


    String idListStringFormation(List<AppData> entryList){
        String idListString="";
        for(AppData appData:entryList){
            idListString+=appData.getId()+",";
        }
        return idListString;
    }

    public void addAppDataInfo(List<AppData> entryList, JSONObject jsonObject){
        try {
            int i=0;
            if(entryList.size()==(int)jsonObject.get("resultCount")) {
                for (AppData appData : entryList) {
                    appData.averageUserRating = (double) jsonObject.getJSONArray("results").getJSONObject(i++).get("averageUserRating");
                    System.out.println(i+"  "+appData.id + "  " + appData.name + "  " + appData.averageUserRating);
                    appData.averageUserRatingForCurrentVersion = (double) jsonObject.getJSONArray("results").getJSONObject(i++).get("averageUserRatingForCurrentVersion");
                }
            }else{
                System.out.println("the size of app data list does not equal to the amount of return jsonObject!");
            }



        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}
