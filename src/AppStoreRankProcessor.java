import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenhao on 2/5/16.
 */


public class AppStoreRankProcessor implements PageProcessor {
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    @Override
    public void process(Page page) {

        List<String> appNames=page.getHtml().xpath("//*[@id=\"main\"]/section/ul/li/h3/a/text()").all();
        List<String> appIds=page.getHtml().links().regex("id[0-9]{8,11}").replace("id","").all();


        appIds= removeDuplicate(appIds);

        int i=1;
        for(String name:appNames){

            System.out.println(i+"  "+name);
            i++;
        }

        int j=1;
        for(String id:appIds){
            System.out.println(j+"  "+id);
            j++;
        }

        page.putField("nameList",appNames);
        page.putField("idList",appIds);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public List removeDuplicate(List originalList){
        HashSet<String> hashSet=new HashSet<>();
        List<String> newList= new ArrayList<>();
        for(Iterator iterator=originalList.iterator();iterator.hasNext();){
            String element=(String)iterator.next();
            if(hashSet.add(element)){
                newList.add(element);
            }
        }
        originalList.clear();
        originalList.addAll(newList);
        return originalList;
    }




    public static void main(String[] args) {

        Spider.create(new AppStoreRankProcessor())
                .addUrl("http://www.apple.com/cn/itunes/charts/paid-apps/")
                .addPipeline(new ObjConstruction())
                .thread(1)
                .run();
    }

}
