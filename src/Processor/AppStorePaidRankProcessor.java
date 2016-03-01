package Processor;

import BasicData.AppData;
import Controller.AppInfoController;
import Downloader.DataDownloader;
import Pipeline.AppStorePaidRankPipeline;
import Utils.Toolkit;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;

/**
 * Created by chenhao on 2/5/16.
 */


public class AppStorePaidRankProcessor implements PageProcessor {

    public static final String PAID_PAGE_URL = "http://www.apple.com/cn/itunes/charts/paid-apps/";
    public static final String FREE_PAGE_URL = "http://www.apple.com/cn/itunes/charts/free-apps/";
    private Site site = Site.me().setCycleRetryTimes(3).setSleepTime(1000).setTimeOut(20000);
    private boolean isFirstPage=true;
    public AppStorePaidRankProcessor() {
        System.out.println("Processor.AppStorePaidRankProcessor Start!");
    }

    public static void main(String args[]) {
        AppInfoController appInfoController = new AppInfoController();
        AppStorePaidRankProcessor appStorePaidRankProcessor = new AppStorePaidRankProcessor();
        DataDownloader dataDownloader = new DataDownloader();

        Spider.create(appStorePaidRankProcessor)
                .addUrl(AppStorePaidRankProcessor.PAID_PAGE_URL)
                .addPipeline(new AppStorePaidRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(dataDownloader)
                .run();

        List<AppData> dataList = appInfoController.fetchAppInfo();
        if (dataList != null) {
            int i=1;
            for (AppData appData : dataList) {
                System.out.println(i+"  "+appData.id + "  " + appData.ranking + "  " + appData.rankFloatNum + "  " + appData.rankType + " " + appData.averageUserRating + "  " + appData.userRatingCount + "  "
                        + appData.userRatingCountForCurrentVersion + "  " + appData.getUserRatingCount() + " " + appData.getScrapeTime());
                i++;
            }
        }
    }

    @Override
    public void process(Page page) {

        List<String> appIdList = page.getHtml().links().regex("id[0-9]{8,11}").replace("id", "").all();
        appIdList = Toolkit.removeDuplicate(appIdList);
        if(isFirstPage){
            page.addTargetRequest(FREE_PAGE_URL);
            isFirstPage=false;
        }

        //the last app is the "Apple Store" app, remove it!
        appIdList.remove(appIdList.size() - 1);
        page.putField("paidIdList", appIdList);

    }

    @Override
    public Site getSite() {
        return site;
    }

}
