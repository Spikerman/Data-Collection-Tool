/**
 * Created by chenhao on 6/4/16.
 */
public class Testing {

    public static void main(String args[]) {
        AppReviewCrawler appReviewCrawler =new AppReviewCrawler();
        appReviewCrawler.buildCandidateClusterMap();
        String appId="1111925955";
        appReviewCrawler.fetchAppReviewData(0,appId);
    }
}
