import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by chenhao on 2/10/16.
 */
public class ReviewPageProcessor implements PageProcessor {
    public static final String APP_STORE_REVIEW_URL
            = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/customerReviews?displayable-kind=11&id=414478124&page=1&sort=4";
    public List<Review> reviewList = new ArrayList<>();

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000)
            .setUserAgent("iTunes/12.3.2 (Macintosh; Intel Mac OS X 10.11.3) AppleWebKit/533.21.1")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh; q=0.50");

    public ReviewPageProcessor() {
        System.out.println("ReviewPageProcessor Start!");
    }

    public static void main(String args[]) {
        Spider.create(new ReviewPageProcessor())
                .addUrl(ReviewPageProcessor.APP_STORE_REVIEW_URL)
                .thread(2)
                .run();
    }

    @Override
    public void process(Page page) {
        reviewList=getReviewsFromPage("414478124",page);
        consoleOutPut(reviewList);

    }

    @Override
    public Site getSite() {
        return site;
    }

    public Review getReview(String appId, Element titleElement, Element review, Element user) throws ParseException {
        String starsString = titleElement.nextElementSibling().attr("aria-label"); //string that contains number of stars e.g. 1星
        String title = titleElement.text(); // string contains a title
        String reviewBody = review.text(); // review itself
        String userInfo = user.text(); // string contains nickname, version of the app and date that can be splitted by dash e.g. 评论人： 魅影伈 - 版本 6.3.13 - 2016年02月10日
        double rate = Double.parseDouble(starsString.substring(0, 1));
        String[] info = userInfo.split("-");
        String version = info[info.length - 2].trim().split(" ")[1];
        String author = info[info.length - 3];
        Date date;
        String dateString = info[info.length - 1].trim();
        date=Toolkit.chineseDateConvert(dateString);
        return new Review(appId, rate, title, reviewBody, date, version, author);

    }

    public List<Review> getReviewsFromPage(String appId, Page page) {
        List<Review> reviewList = new ArrayList<>();
        Document document = page.getHtml().getDocument();
        try {
            Elements titles = document.getElementsByClass("customerReviewTitle");
            Elements reviews = document.getElementsByClass("content");
            Elements users = document.getElementsByClass("user-info");
            Elements pageNumbers = document.getElementsByAttribute("total-number-of-pages");
            for (int i = 0; i < reviews.size(); i++) {
                reviewList.add(getReview(appId, titles.get(i), reviews.get(i), users.get(i)));
            }
        }catch(ParseException e){
            e.printStackTrace();
        }
        return reviewList;
    }

    public void consoleOutPut(List<Review> reviewList){
        DateFormat formatterCH = new SimpleDateFormat("yyyy.MM.dd");

        for(Review review:reviewList){
            //System.out.printf("%-20s",review.getTitle());
            //System.out.printf("%-20s",review.getBody());
            System.out.printf("%-50s",review.getAuthor());
            System.out.printf("%-50s",review.getRate());
            System.out.printf("%-50s",review.getVersion());
            System.out.printf("%tF",review.getDate());
            System.out.println();

        }
    }


}
