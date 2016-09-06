import Downloader.ReviewDataDownLoader;
import Processor.ReviewPageProcessor;
import sun.applet.Main;
import us.codecraft.webmagic.Spider;

/**
 * Created by chenhao on 6/4/16.
 */
public class Testing {

    public static void main(String args[]) {
        MainCrawler mainCrawler=new MainCrawler();
        mainCrawler.buildCandidateClusterMap();
        String appId="1111925955";
        mainCrawler.fetchAppReviewData(0,appId);
    }
}
