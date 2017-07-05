package Yufei.CS504.homework;


import java.io.Serializable;
import java.util.List;

public class Ad implements Serializable {
    private static final long serialVersionUID = 1L;
    public int adId;
    public int campaignId;
    public List<String> keyWords;
    public double relevanceScore;
    public double pClick;
    public double bidPrice;
    public double rankScore;
    public double qualityScore;
    public double costPerClick;
    public int position;//1: top , 2: bottom
    public String title; // required
    public double price; // required
    public String thumbnail; // required
    public String description; // required
    public String brand; // required
    public String detail_url; // required
    public String query; //required
    public int query_group_id;
    public String category;

    @Override
    public String toString() {
        return "adId: " + adId +
                " campaignId: " + campaignId +
                " keywords: " + keyWords.toString() +
                " relevanceScore: " + relevanceScore +
                " pClick: " + pClick +
                " bidPrice: " + bidPrice +
                " rankScore: " + rankScore +
                " qualityScore: " + qualityScore +
                " costPerClick: " + costPerClick +
                " position: " + position +
                " title: " + title +
                " price: " + price +
                " thumbnail: " + thumbnail +
                " description" + description +
                " brand: " + brand +
                " detail_url: " + detail_url +
                " query: " + query +
                " query_group_id: " + query_group_id +
                " category: " + category;
    }
}
