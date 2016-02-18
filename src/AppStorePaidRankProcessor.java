import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

/**
 * Created by chenhao on 2/5/16.
 */


public class AppStorePaidRankProcessor implements PageProcessor {

    public static final String PAGE_URL ="http://www.apple.com/cn/itunes/charts/paid-apps/";
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(20000);

    public AppStorePaidRankProcessor() {
        System.out.println("AppStorePaidRankProcessor Start!");
    }

    @Override
    public void process(Page page) {

        List<String> appIdList = page.getHtml().links().regex("id[0-9]{8,11}").replace("id", "").all();

        appIdList = Toolkit.removeDuplicate(appIdList);

        //the last app is the "Apple Store" app, remove it!
        appIdList.remove(appIdList.size() - 1);
        page.putField("paidIdList", appIdList);

    }

    @Override
    public Site getSite() {
        return site;
    }





}
