/**
 * Created by chenhao on 2/5/16.
 */
public class AppData {
    public String id;
    public int ranking;
    public double averageUserRating;
    public double averageUserRatingForCurrentVersion;
    public int userRatingCount;
    public int userRatingCountForCurrentVersion;


    public AppData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }
}
