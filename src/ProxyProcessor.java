import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenhao on 2/22/16.
 */
public class ProxyProcessor implements PageProcessor {
    public static final String PROXY_URL = "http://www.kuaidaili.com/free/inha/%d/";
    public String INITIAL_URL;
    private List<Proxy> proxyList = new ArrayList<>();
    private Site site = Site.me().setCycleRetryTimes(5).setSleepTime(2000).setTimeOut(200000);
    private int scrapePageCount = 5;

    public ProxyProcessor() {
        INITIAL_URL = String.format(PROXY_URL, 1);
        System.out.println("ProxyProcessor Start!");
    }

    public static void main(String args[]) {
        ProxyProcessor proxyProcessor = new ProxyProcessor();
        proxyProcessor.setScrapePageCount(10);
        Spider.create(proxyProcessor)
                .addUrl(proxyProcessor.INITIAL_URL)
                .thread(1)
                .run();


        for (Proxy proxy : proxyProcessor.proxyList) {
            System.out.println(proxy.getIp() + "  " + proxy.getPort() + "  " + proxy.getType() + "  " + proxy.getResponseTime());
        }
    }

    public int getScrapePageCount() {
        return scrapePageCount;
    }

    public void setScrapePageCount(int scrapePageCount) {
        this.scrapePageCount = scrapePageCount;
    }

    public List<Proxy> getProxyList() {
        return proxyList;
    }

    @Override
    public Site getSite() {
        return site;
    }

    public void process(Page page) {
        Document document = page.getHtml().getDocument();
        Element tbody = document.getElementsByTag("tbody").first();
        Elements proxyInfoRows = tbody.getElementsByTag("tr");
        for (int i = 0; i < proxyInfoRows.size(); i++) {
            proxyList.add(new Proxy(proxyInfoRows.get(i).child(0).text(), proxyInfoRows.get(i).child(1).text(), proxyInfoRows.get(i).child(3).text(), proxyInfoRows.get(i).child(5).text()));
        }
        page.addTargetRequests(addUrls(scrapePageCount));
    }

    public List<String> addUrls(int pageCount) {
        List<String> urlList = new ArrayList<>();
        for (int i = 1; i < pageCount; i++) {
            String urlString = String.format(PROXY_URL, i + 1);
            urlList.add(urlString);
        }
        return urlList;

    }

}
