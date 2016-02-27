package Processor;

import BasicData.AppData;
import Controller.AppInfoController;
import Downloader.DataDownloader;
import Pipeline.FloatUpRankPipeline;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.List;


//get the app name and detail information from the aso100.com

public class FloatUpRankPageProcessor implements PageProcessor {

    public static final String PAGE_URL = "http://aso100.com/index.php/rank/float?float=up";
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000).setTimeOut(200000);
    private int size = 200;

    public FloatUpRankPageProcessor() {
        System.out.println("Processor.FloatUpRankPageProcessor Start!");
    }

    public static void main(String args[]) {
        FloatUpRankPageProcessor floatUpRankPageProcessor = new FloatUpRankPageProcessor();
        AppInfoController appInfoController = new AppInfoController();

        floatUpRankPageProcessor.setSize(50);

        Spider.create(floatUpRankPageProcessor)
                .addUrl(FloatUpRankPageProcessor.PAGE_URL)
                .addPipeline(new FloatUpRankPipeline(appInfoController))
                .thread(1)
                .setDownloader(new DataDownloader())
                .run();

        List<AppData> appDataList = appInfoController.getAppDataList();
        int i=1;
        for (AppData x : appDataList) {
            System.out.println(i+++"  "+x.getId());
        }

    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void process(Page page) {

        //get app id from the list
        List<String> appIdList = page.getHtml().links().regex("appid/[0-9]*").replace("appid/", "").all();
        page.putField("upIdList", appIdList);
        page.putField("size", size);

    }

    @Override
    public Site getSite() {
        return site;
    }

}
