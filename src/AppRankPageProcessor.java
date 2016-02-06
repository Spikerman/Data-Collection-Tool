import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;


public class AppRankPageProcessor implements PageProcessor {

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);

    @Override
    public void process(Page page) {

        // 部分二：定义如何抽取页面信息，并保存下来

        String currentUrl= page.getUrl().toString();
        System.out.println(currentUrl);

        String headUrl=currentUrl.substring(0,28)+"app/rank?";
        System.out.println(headUrl);


        List<String> appNames=page.getHtml().xpath("//h5/text()").all();



        List<String> appPartUrls=page.getHtml().links().regex("/index.php/app/rank\\?appid=.*").all();
        for(String name:appNames){
            System.out.println(name);
        }

        List<String> realUrlList = new ArrayList<>();

        for(String url:appPartUrls){
            String realUrl=headUrl+url.substring(20);
            realUrlList.add(realUrl);
            System.out.println(realUrl);
        }

        //List<String> appIds = new ArrayList<>();

        page.putField("id",page.getHtml().xpath("//*[@id=\"app\"]/div/div[2]/div[1]/div/div[1]/div[3]/p[2]/a/text()").toString());
        System.out.println(page.getHtml().xpath("//*[@id=\"app\"]/div/div[2]/div[1]/div/div[1]/div[3]/p[2]/a/text()").toString());

        // 部分三：从页面发现后续的url地址来抓取
        page.addTargetRequests(realUrlList);
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {

        Spider.create(new AppRankPageProcessor())
                //从"https://github.com/code4craft"开始抓
                .addUrl("http://aso100.com/index.php/rank/float?float=up")
                //开启1个线程抓取
                .thread(1)
                //启动爬虫
                .run();
    }

}
