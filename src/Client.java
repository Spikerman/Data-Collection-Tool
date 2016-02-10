import us.codecraft.webmagic.Spider;

/**
 * Created by chenhao on 2/7/16.
 */

public class Client {


    public static void main(String[] args) {

        Spider.create(new AppStorePaidRankProcessor())
                .addUrl(AppStorePaidRankProcessor.PAGE_URL)
                .addPipeline(new PaidRankPipeline())
                .thread(1)
                .run();

//        Spider.create(new FloatUpRankPageProcessor())
//                .addUrl(FloatUpRankPageProcessor.PAGE_URL)
//                .addPipeline(new UpRankPipeline())
//                .thread(1)
//                .run();
    }
}
