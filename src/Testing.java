import Downloader.ReviewDataDownLoader;
import Processor.ReviewPageProcessor;
import us.codecraft.webmagic.Spider;

/**
 * Created by chenhao on 6/4/16.
 */
public class Testing {

    public static void main(String args[]) {
        String appId = "893217990";
        ReviewDataDownLoader reviewDataDownloader = new ReviewDataDownLoader();

        ReviewPageProcessor reviewPageProcessor = new ReviewPageProcessor(appId);

        Spider.create(reviewPageProcessor)
                .addUrl(ReviewPageProcessor.INITIAL_URL)
                .thread(5)
                .setDownloader(reviewDataDownloader)
                .run();
    }
}
