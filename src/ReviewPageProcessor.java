import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;


/**
 * Created by chenhao on 2/10/16.
 */
public class ReviewPageProcessor implements PageProcessor {
    public static final String APP_STORE_REVIEW_URL
            = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/customerReviews?displayable-kind=11&id=414478124&page=1&sort=4";

    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000)
            .setUserAgent("iTunes/10.3.1 (Macintosh; Intel Mac OS X 10.6.8) AppleWebKit/533.21.1")
            .addHeader("X-Apple-Store-Front", "143465,12")
            .addHeader("Accept-Language", "en-us, en, zh;q=0.50");

    public ReviewPageProcessor() {
        System.out.println("ReviewPageProcessor Start!");
    }



    @Override
    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        Elements reviews = document.getElementsByClass("content");
        Elements users = document.getElementsByClass("user-info");
        Elements pageNumbers = document.getElementsByAttribute("total-number-of-pages");

        System.out.println(reviews.size());

        for (Element element : reviews) {
            System.out.println(element.text());
        }


        System.out.println(users.size());
        for (Element element : users) {
            System.out.println(element.text());
        }

        if (pageNumbers.size() != 0) { // no reviews
            int totalNumOfPages = Integer.valueOf(pageNumbers.get(0).attr("total-number-of-pages"));
            System.out.println("totalNumOfPages:  "+totalNumOfPages);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String args[]) {
        Spider.create(new ReviewPageProcessor())
                .addUrl(ReviewPageProcessor.APP_STORE_REVIEW_URL)
                .thread(1)
                .run();
    }

}
