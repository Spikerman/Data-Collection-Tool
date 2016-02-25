/**
 * Created by chenhao on 2/7/16.
 */

public class Client {


    public static void main(String[] args) {

        DataCrawler dataCrawler = new DataCrawler();
        dataCrawler.fetchAllAppId();
        dataCrawler.fetchAllAppInfo();
        dataCrawler.fetchProxy();
        dataCrawler.fetchAppReviewAuthorInfo();

    }
}
