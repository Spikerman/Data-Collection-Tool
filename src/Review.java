import java.util.Date;

/**
 * Created by chenhao on 2/11/16.
 */
public class Review implements Comparable<Review> {
    private String id;
    private String appId;
    private double rate;
    private String title;
    private String body;
    private Date date;
    private String version;
    private String author;

    public Review(String appId, double rate, String title, String body, Date date, String version, String author) {
        this.appId = appId;
        this.rate = rate;
        this.title = title;
        this.body = body;
        this.date = date;
        this.version = version;
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public String getAppId() {
        return appId;
    }

    public double getRate() {
        return rate;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Date getDate() {
        return date;
    }

    public String getVersion() {
        return version;
    }

    public int compareTo(Review review) {
        if (review.date != null) {
            return review.date.compareTo(date);
        } else {
            return -1;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        if (!appId.equals(review.appId)) return false;
//        if (!id.equals(review.id)) return false;  //review id has not been defined yet
        if (!author.equals(review.author)) return false;
        if (Double.compare(review.rate, rate) != 0) return false;
        if (title != null ? !title.equals(review.title) : review.title != null) return false;
        if (body != null ? !body.equals(review.body) : review.body != null) return false;
        if (date != null ? !date.equals(review.date) : review.date != null) return false;
        return !(version != null ? !version.equals(review.version) : review.version != null);

    }


    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (Integer.parseInt(appId) ^ (Integer.parseInt(appId) >>> 32));
        temp = Double.doubleToLongBits(rate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (author != null ? author.hashCode() : 0);
        return result;
    }


}
