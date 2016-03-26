package Model;

/**
 * Created by yannd on 2016-03-25.
 */
public class Recommendation {
    private String recommender;
    private String recommendee;
    private String serieName;

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }

    public String getRecommendee() {
        return recommendee;
    }

    public void setRecommendee(String recommendee) {
        this.recommendee = recommendee;
    }

    public String getSerieName() {
        return serieName;
    }

    public void setSerieName(String serieName) {
        this.serieName = serieName;
    }
}
