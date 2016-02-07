import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by chenhao on 2/5/16.
 */


public class AppStorePaidRankProcessor implements PageProcessor {

    public static final String PAGE_URL ="http://www.apple.com/cn/itunes/charts/paid-apps/";
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    public AppStorePaidRankProcessor() {
        System.out.println("AppStorePaidRankProcessor Start!");
    }

    @Override
    public void process(Page page) {


        List<String> appNames = page.getHtml().xpath("//*[@id=\"main\"]/section/ul/li/h3/a/text()").all();
        List<String> appIdList = page.getHtml().links().regex("id[0-9]{8,11}").replace("id", "").all();


        appIdList = removeDuplicate(appIdList);

        //the last app is the Apple Store, remove it!
        appIdList.remove(appIdList.size() - 1);

        //the last app is the Apple Store, remove it!

        page.putField("paidIdList", appIdList);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public List<String> removeDuplicate(List originalList) {
        HashSet<String> hashSet = new HashSet<>();
        List<String> newList = new ArrayList<>();
        for (Iterator iterator = originalList.iterator(); iterator.hasNext(); ) {
            String element = (String) iterator.next();
            if (hashSet.add(element)) {
                newList.add(element);
            }
        }
        originalList.clear();
        originalList.addAll(newList);
        return originalList;
    }

}
