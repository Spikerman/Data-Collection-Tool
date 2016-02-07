/**
 * Created by chenhao on 2/5/16.
 */
public class AppData {
    public String name;
    public String id;
    public int rank;
    public double averageUserRating;
    public double averageUserRatingForCurrentVersion;
    public int userRatingCount;
    public int userRatingCountForCurrentVersion;

    public AppData(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public AppData(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
