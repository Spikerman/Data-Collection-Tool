import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.ParseException;
import java.util.*;


/**
 * Created by chenhao on 2/10/16.
 */
public class ReviewPageProcessor implements PageProcessor {
    public static final String APP_STORE_REVIEW_URL
            = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/customerReviews?displayable-kind=11&id=%s&page=%d&sort=4";

    public static String INITIAL_URL;
    public static String id;
    public static int pageCount;
    public static Elements pageNumbers;
    public static List<String> pageUrls;
    public Set<Review> reviewSet;

    private Site site = Site.me().setRetryTimes(5).setSleepTime(1000)
            .setCharset("utf-8")
            .setUserAgent("iTunes/12.3.2 (Macintosh; Intel Mac OS X 10.11.3) AppleWebKit/533.21.1")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh; q=0.50");

    public ReviewPageProcessor(String entryId) {
        INITIAL_URL = String.format(APP_STORE_REVIEW_URL, entryId, 1);
        id = entryId;

        //in order to keep thread-safe
        reviewSet = Collections.synchronizedSet(new HashSet<>());

        System.out.println("ReviewPageProcessor Start!");

    }

    public static void main(String args[]) {
        Spider.create(new ReviewPageProcessor("931179407"))
                .addUrl(ReviewPageProcessor.INITIAL_URL)
                .addPipeline(new ReviewPagePipeline())
                .thread(20)
                .run();

    }

    @Override
    public void process(Page page) {

        Document document = page.getHtml().getDocument();
        pageNumbers = document.getElementsByAttribute("total-number-of-pages");

        if (pageNumbers.size() != 0) {
            pageCount = Integer.valueOf(pageNumbers.get(0).attr("total-number-of-pages"));
            pageUrls = addUrls(pageCount);
            page.addTargetRequests(pageUrls);
            List reviewList = getReviewsFromPage(id, page);
            reviewList = Toolkit.removeDuplicate(reviewList);
            consoleOutPut(reviewList);

            reviewSet.addAll(reviewList);

            System.out.println("-------------------------------------------------------");
            System.out.println("total number: " + reviewSet.size());
            System.out.println("-------------------------------------------------------");
        }
        page.putField("results", reviewSet);
    }

    @Override
    public Site getSite() {
        return site;
    }

    //construct the review object
    public Review getReview(String appId, String reviewId, Element titleElement, Element review, Element user, String userId) throws ParseException {

        String starsString = titleElement.nextElementSibling().attr("aria-label"); //string that contains number of stars e.g. 1星
        String title = titleElement.text(); // string contains a title
        String reviewBody = review.text(); // review itself
        String userInfo = user.text(); // string contains nickname, version of the app and date that can be splitted by dash e.g. 评论人： 魅影伈 - 版本 6.3.13 - 2016年02月10日
        double rate = Double.parseDouble(starsString.substring(0, 1));
        String[] info = userInfo.split("-");
        String version = info[info.length - 2].trim().split(" ")[1];

        Date date;
        String dateString = info[info.length - 1].trim();
        date = Toolkit.chineseDateConvert(dateString);
        return new Review(appId, reviewId, rate, title, reviewBody, date, version, userId);
    }

    //get all reviews from the current page
    public List<Review> getReviewsFromPage(String appId, Page page) {
        List<Review> reviewList = new ArrayList<>();
        Document document = page.getHtml().getDocument();

        List<String> userProfileIdList = page.getHtml().links().regex("userProfileId=[0-9]*").replace("userProfileId=", "").all();
        List<String> reviewIdList = page.getHtml().regex("userReviewId=[0-9]*").replace("userReviewId=", "").all();
        List x=Toolkit.testRemove(reviewIdList);

        try {
            Elements titles = document.getElementsByClass("customerReviewTitle");
            Elements reviews = document.getElementsByClass("content");
            Elements users = document.getElementsByClass("user-info");

            for (int i = 0; i < reviews.size(); i++) {
                reviewList.add(getReview(appId, reviewIdList.get(i), titles.get(i), reviews.get(i), users.get(i), userProfileIdList.get(i)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reviewList;
    }

    public void consoleOutPut(Set<Review> reviewList) {

        for (Review review : reviewList) {
            System.out.printf("%-50s %-50s %-50s %tF", review.getAuthorId(), review.getRate(), review.getVersion(), review.getDate());
            System.out.println();
        }
    }

    //output every review content
    public void consoleOutPut(List<Review> reviewList) {

        for (Review review : reviewList) {
            System.out.printf("%-50s %-50s  %-50s %-50s %tF", review.getAuthorId(), review.getId(), review.getRate(), review.getVersion(), review.getDate());
            System.out.println();

        }
    }

    //add the following review page url, start from the second page
    public List<String> addUrls(int pageCount) {
        List<String> urlList = new ArrayList<>();
        for (int i = 1; i < pageCount; i++) {
            String urlString = String.format(APP_STORE_REVIEW_URL, id, i + 1);
            urlList.add(urlString);
        }
        return urlList;

    }


}
