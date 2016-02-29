package BasicData;

import java.util.Date;

/**
 * Created by chenhao on 2/5/16.
 */
public class AppData {
    public static final String topfree = "topfree";
    public static final String topPaid = "toppaid";
    public static final String topFlowUp = "topflowup";
    public static final String topFlowDown = "topflowdown";
    public static final String topFreeGame = "topfreegame";
    public static final String topPaidGame = "toppaidgame";
    public String id;
    public int ranking;
    public int rankFloatNum = 0;
    public double averageUserRating;
    public double averageUserRatingForCurrentVersion;
    public int userRatingCount;
    public int userRatingCountForCurrentVersion;
    public String rankType;
    private Date scrapeTime = new Date();
    public String currentVersion;
    public String currentVersionReleaseDate;

    public AppData(String id) {
        this.id = id;
    }

    public AppData(String id, String rankType) {
        this.id = id;
        this.rankType = rankType;
    }

    public AppData(String id, int ranking, int rankFloatNum, String rankType) {
        this.id = id;
        this.ranking = ranking;
        this.rankType = rankType;
        this.rankFloatNum = rankFloatNum;
    }

    public AppData(String id, int ranking,  String rankType) {
        this.id = id;
        this.ranking = ranking;
        this.rankType = rankType;
    }


    public double getAverageUserRating() {
        return averageUserRating;
    }

    public double getAverageUserRatingForCurrentVersion() {
        return averageUserRatingForCurrentVersion;
    }

    public int getUserRatingCount() {
        return userRatingCount;
    }

    public int getUserRatingCountForCurrentVersion() {
        return userRatingCountForCurrentVersion;
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

    public String getRankType() {
        return rankType;
    }

    public void setRankType(String rankType) {
        this.rankType = rankType;
    }

    public Date getScrapeTime() {
        return scrapeTime;
    }
}
