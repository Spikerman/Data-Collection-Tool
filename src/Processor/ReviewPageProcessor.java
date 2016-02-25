package Processor;

import BasicData.Proxy;
import BasicData.Review;
import Controller.DbController;
import Pipeline.ReviewPagePipeline;
import Utils.Toolkit;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by chenhao on 2/10/16.
 */
public class ReviewPageProcessor implements PageProcessor {

    public static final String APP_STORE_REVIEW_URL
            = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/customerReviews?displayable-kind=11&id=%s&page=%d&sort=4";
    public static String INITIAL_URL;
    public List<String> pageUrls;
    //keep thread-safe
    public Set<Review> reviewSet = Collections.synchronizedSet(new HashSet<>());
    public Site site = Site.me().setCycleRetryTimes(5).setSleepTime(1500).setTimeOut(150000)
            .setCharset("utf-8")
            .setUserAgent("iTunes/12.2.1 (Macintosh; Intel Mac OS X 10.11.3) AppleWebKit/601.4.4")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh; q=0.50");
    private String id;
    private int pageCount;
    private Elements pageNumbers;
    private List<Proxy> proxyList;
    private boolean isFirstPage = true;
    private Proxy proxy = null;
    private List<String> appIdList;
    private Pattern userIdPattern = Pattern.compile("\\d+");
    private Pattern reviewIdPattern = Pattern.compile("\\d+");

    public ReviewPageProcessor(String entryId) {
        INITIAL_URL = String.format(APP_STORE_REVIEW_URL, entryId, 1);
        id = entryId;
        System.out.println("Processor.ReviewPageProcessor Start!");
    }

    public ReviewPageProcessor(String entryId, Proxy proxy) {
        INITIAL_URL = String.format(APP_STORE_REVIEW_URL, entryId, 1);
        id = entryId;
        this.proxy = proxy;
        System.out.println("Processor.ReviewPageProcessor Start!");
    }

    public ReviewPageProcessor(List<String> appIdList) {
        this.appIdList = appIdList;
    }

    public static void main(String args[]) {

        String sql = "insert into BasicData.Review (id,appId,rate,version,date) values(?,?,?,?,?)";
        DbController dbHelper = new DbController();
        dbHelper.setInsertReviewPst(sql);
        ReviewPageProcessor reviewPageProcessor = new ReviewPageProcessor("685872176");
        Spider.create(reviewPageProcessor)
                .addUrl(ReviewPageProcessor.INITIAL_URL)
                .addPipeline(new ReviewPagePipeline())
                .thread(10)
                .run();

        Set<Review> reviewSet = reviewPageProcessor.getReviewSet();
        for (Review review : reviewSet) {
            try {
                dbHelper.insertReviewPst.setString(1, review.getId());
                dbHelper.insertReviewPst.setString(2, review.getAppId());
                dbHelper.insertReviewPst.setDouble(3, review.getRate());
                dbHelper.insertReviewPst.setString(4, review.getVersion());
                dbHelper.insertReviewPst.setDate(5, new java.sql.Date(review.getDate().getTime()));
                dbHelper.insertReviewPst.executeUpdate();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    public void setProxyList(List<Proxy> proxyList) {
        this.proxyList = proxyList;
    }

    public Set<Review> getReviewSet() {
        return reviewSet;
    }

    @Override
    public void process(Page page) {

        Document document = page.getHtml().getDocument();
        pageNumbers = document.getElementsByAttribute("total-number-of-pages");

        if (pageNumbers.size() != 0) {
            if (isFirstPage) {
                pageCount = Integer.valueOf(pageNumbers.get(0).attr("total-number-of-pages"));
                pageUrls = addUrls(pageCount);
                page.addTargetRequests(pageUrls);
            }
            List reviewList = getReviewsFromPage(id, page);
            consoleOutPut(reviewList);
            reviewSet.addAll(reviewList);

            System.out.println("-------------------------------------------------------");
            System.out.println("total number: " + reviewSet.size());
            System.out.println("-------------------------------------------------------");

            isFirstPage = false;
        }
    }

    @Override
    public Site getSite() {
        return site;
    }


    public Review getReview(String appId, Element customerReview) throws ParseException {

        Element user = customerReview.getElementsByClass("user-info").first();
        String reviewIdString = customerReview.getElementsByClass("report-a-concern").first().attr("report-a-concern-fragment-url");
        String rateString = customerReview.getElementsByClass("rating").first().attr("aria-label"); //string that contains number of stars e.g. 1星
        String userInfo = user.text(); // string contains nickname, version of the app and date that can be splitted by dash e.g. 评论人： 魅影伈 - 版本 6.3.13 - 2016年02月10日
        String userIdString = user.child(0).attr("href");

        Matcher userIdMatcher = userIdPattern.matcher(userIdString);
        userIdMatcher.find();
        String userId = userIdMatcher.group();

        Matcher reviewIdMatcher = reviewIdPattern.matcher(reviewIdString);
        reviewIdMatcher.find();
        String reviewId = reviewIdMatcher.group();


        String[] info = userInfo.split("-");
        String version = info[info.length - 2].trim().split(" ")[1];
        double rate = Double.parseDouble(rateString.substring(0, 1));
        String dateString = info[info.length - 1].trim();
        Date date = Toolkit.chineseDateConvert(dateString);

        return new Review(appId, reviewId, rate, date, version, userId);


    }


    //get all reviews from the current page
    public List<Review> getReviewsFromPage(String appId, Page page) {
        List<Review> reviewList = new ArrayList<>();
        Document document = page.getHtml().getDocument();
        Elements customerReviews = document.getElementsByClass("customer-review");

        try {
            for (int i = 0; i < customerReviews.size(); i++) {
                reviewList.add(getReview(appId, customerReviews.get(i)));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return reviewList;
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

    //construct the review object
    public Review getReview(String appId, String reviewId, Element titleElement, Element user, String userId) throws ParseException {

        String rateString = titleElement.nextElementSibling().attr("aria-label"); //string that contains number of stars e.g. 1星
        String userInfo = user.text(); // string contains nickname, version of the app and date that can be splitted by dash e.g. 评论人： 魅影伈 - 版本 6.3.13 - 2016年02月10日
        double rate = Double.parseDouble(rateString.substring(0, 1));
        String[] info = userInfo.split("-");
        String version = info[info.length - 2].trim().split(" ")[1];
        String dateString = info[info.length - 1].trim();
        Date date = Toolkit.chineseDateConvert(dateString);
        return new Review(appId, reviewId, rate, date, version, userId);
    }


}
