/**
 * Created by chenhao on 2/7/16.
 */


//获取可疑应用的 ID 以及其基本的数据信息，比如评分，评论数量，排名等
public class CollectionClient {
    public static void main(String[] args) {
        SuspiciousAppCollector suspiciousAppCollector = new SuspiciousAppCollector();
        suspiciousAppCollector.fetchAllAppId();
        suspiciousAppCollector.fetchAllAppInfo();
        suspiciousAppCollector.fetchProxy();
        suspiciousAppCollector.fetchAppReviewAuthorInfo();
    }
}
