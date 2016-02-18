import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;


//get the app name and detail information from the aso100.com

public class FloatUpRankPageProcessor implements PageProcessor {

    public static final String PAGE_URL = "http://aso100.com/index.php/rank/float?float=up";
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(200000);

    public FloatUpRankPageProcessor() {
        System.out.println("FloatUpRankPageProcessor Start!");
    }

    @Override
    public void process(Page page) {

        //get app id from the list
        List<String> appIdList = page.getHtml().links().regex("appid=[0-9]{8,11}&").replace("appid=", "").replace("&", "").all();
        page.putField("upIdList", appIdList);

    }

    @Override
    public Site getSite() {
        return site;
    }


}
