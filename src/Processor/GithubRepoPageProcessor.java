package Processor;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

public class GithubRepoPageProcessor implements PageProcessor {

    public static String reviewPagelink = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/customerReviews?displayable-kind=11&id=368677368&page=1&sort=4";
    public static String userPagelink = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/viewUsersUserReviews?userProfileId=481939717";

    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    public Site site = Site.me().setCycleRetryTimes(20).setSleepTime(2000).setTimeOut(150000)
            .setCharset("utf-8")
            .setUserAgent("iTunes/12.2.1 (Macintosh; Intel Mac OS X 10.11.3) AppleWebKit/601.4.4")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh; q=0.50");

    public static void main(String[] args) {
        Spider.create(new GithubRepoPageProcessor())
                //从"https://github.com/code4craft"开始抓
                .addUrl(userPagelink)
                //开启1个线程抓取
                .thread(1)
                //启动爬虫
                .run();
    }

    @Override
    // process是定制爬虫逻辑的核心接口，在这里编写抽取逻辑

    public void process(Page page) {
        System.out.println(page.getHtml());
    }

    @Override
    public Site getSite() {
        return site;
    }
}