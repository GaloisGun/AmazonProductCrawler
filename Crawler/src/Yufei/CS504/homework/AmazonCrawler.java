package Yufei.CS504.homework;

import java.io.StringReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.io.IOException;

import org.apache.log4j.Logger;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;




public class AmazonCrawler {
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.95 Safari/537.36";
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";
    private final String authUser = "bittiger";
    private final String authPassword = "cs504";
    private static int adId = 0;
    final static Logger logger = Logger.getLogger(AmazonCrawler.class);
    private static Set<String> queriesSet = new HashSet<String>();

    private static final Version LUCENE_VERSION = Version.LUCENE_40;
    private static String stopWords = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
    private static String[] stopWordsArray = new String[] {".", ",", "\"", "'", "?", "!", ":", ";", "(", ")", "[", "]", "{", "}", "&", "/", "...", "-", "+", "*", "|", "),"};


    public void initProxy() {
        //System.setProperty("socksProxyHost", "199.101.97.161"); // set socks proxy server
        //System.setProperty("socksProxyPort", "61336"); // set socks proxy port

        System.setProperty("http.proxyHost", "199.101.97.159"); // set proxy server
        System.setProperty("http.proxyPort", "60099"); // set proxy port
        //System.setProperty("http.proxyUser", authUser);
        //System.setProperty("http.proxyPassword", authPassword);
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
    }

    public List<Ad> getProds(String query, double bidPrice, int campaignId, int queryGroupId, int pageNum) {
        List<Ad> adList = new ArrayList<>();
        String url = AMAZON_QUERY_URL + query;
        String pageString = "&page=";

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            //headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Encoding", "gzip, deflate");

            for (int page = 1; page <= pageNum; page++) {
                url = url + pageString + Integer.toString(page);
                if (queriesSet.contains(query)) {
                    continue;
                }
                else {
                    queriesSet.add(query);
                    Document document = Jsoup.connect(url).maxBodySize(0).headers(headers).userAgent(USER_AGENT).timeout(10000).get();
                    Elements result = document.select("li[data-asin]");

                    int resultsLength = result.size();

                    for (int i = 0; i < resultsLength; i++) {
                        int index = getResultIndex(result.get(i));
                        List<String> keywordList = crawlKeywords(document, index);
                        String category = getCrawlerCategory(document);
                        String titleString = getCrawlerTitle(document, index);
                        Ad ad = updateAd(document, result.get(i), index, query, bidPrice, campaignId, queryGroupId);
                        adList.add(ad);
                    }

                }
            }
        }catch (IOException e) {
            e.printStackTrace();
            logger.error("ERRO", e);

        }

        return adList;
    }

    private Ad updateAd(Document document, Element element, int index, String query, double bidPrice, int campaignId, int queryGroupId) {
        Ad ad = new Ad();
        ad.query = query;
        ad.bidPrice = bidPrice;
        ad.campaignId = campaignId;
        ad.query_group_id = queryGroupId;
        ad.brand = crawlBrand(document, index);
        ad.category = crawlCategory(document);
        ad.costPerClick = 0.0;
        ad.description = null;
        ad.title = crawlTitle(document, index);
        ad.detail_url = crawlDetailUrl(document, index);
        ad.keyWords = crawlKeywords(document, index);
        //ad.prodId = crawlProdId(result);
        ad.adId = AmazonCrawler.adId++;
        ad.pClick = 0.0;
        ad.position = 0;
        ad.relevanceScore = 0.0;
        ad.rankScore = 0.0;
        ad.qualityScore = 0.0;
        ad.thumbnail = crawlThumbnail(document, index);
        //ad.price = 0.0;
        ad.price = crawlPrice(document, index);
        //System.out.println("price: " + ad.price);
        return ad;
    }

    private double crawlPrice(Document document, int index) {
        Map<String, String> wholeFractMap = new HashMap<String, String>();
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(2) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");
        wholeFractMap.put(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > span",
                " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(4) > div.a-column.a-span7 > div:nth-child(1) > div:nth-child(3) > a > span.a-color-base.sx-zero-spacing > span > sup.sx-price-fractional");

        try {
            for (String mapEle : wholeFractMap.keySet()) {
                String priceWholePath = "#result_" + Integer.toString(index) + mapEle;
                String priceFracPath = "#result_" + Integer.toString(index) + wholeFractMap.get(mapEle);
                Element wholePriceEle = document.select(priceWholePath).first();
                Element fracPriceEle = document.select(priceFracPath).first();
                if (wholePriceEle != null && fracPriceEle != null) {
                    String wholePriceString = wholePriceEle.text();
                    String fracPriceString = fracPriceEle.text();
                    if (wholePriceString.contains(",")) {
                        wholePriceString = wholePriceString.replace(",", "");
                        return Double.parseDouble(wholePriceString) + Double.parseDouble(fracPriceString) / 100.0;
                    }
                    else {
                        return Double.parseDouble(wholePriceString) + Double.parseDouble(fracPriceString) / 100.0;
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, something wrong!", e );
        }

        return 0.0;
    }

    private String crawlThumbnail(Document document, int index) {
        String imgPart = " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img";
        String thumbnailPath = "#result_" + Integer.toString(index) + imgPart;
        Element thumbnail = document.select(thumbnailPath).first();
        if (thumbnail != null) {
            return thumbnail.attr("src");
        }
        else {
            return null;
        }
    }

    private List<String> crawlKeywords(Document document, int index) {
        List<String> titleList = new ArrayList<>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2");
        List<String> keywords = new ArrayList<>();
        try {
            for (String title : titleList) {
                //String titlePath = "#result_" + Integer.toString(index) + title;
                Element titleEle = document.select("#result_" + Integer.toString(index) + title).first();
                if (titleEle != null) {
                    //System.out.println("titleEle: " + titleEle.text());
                    keywords = cleanedTokenize(titleEle.text());
                    //System.out.println("keywords: " + keywords.toString());
                    break;
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, error occurred in function crawlKeywords", e);
        }
        return keywords;
    }

    private List<String> cleanedTokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringReader reader = new StringReader(input.toLowerCase());
        Tokenizer tokenizer = new StandardTokenizer(LUCENE_VERSION, reader);
        TokenStream tokenStream = new StandardFilter(LUCENE_VERSION, tokenizer);
        tokenStream = new StopFilter(LUCENE_VERSION, tokenStream, getStopwords(stopWords));
        tokenStream = new KStemFilter(tokenStream);
        StringBuilder stringBuilder = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);

        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();

                tokens.add(term);
                stringBuilder.append(term).append(" ");
            }
            tokenStream.end();
            tokenStream.close();
            tokenizer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    private CharArraySet getStopwords(String stopWords) {
        List<String> stopwordsList = new ArrayList<String>();
        for (String stop : stopWords.split(",")) {
            stopwordsList.add(stop.trim());
        }
        for (String stop : stopWordsArray) {
            stopwordsList.add(stop.trim());
        }

        return new CharArraySet(LUCENE_VERSION, stopwordsList, true);
    }

    public static String strJoin(List<String> aArr, String sSep) {
        StringBuilder sbStr = new StringBuilder();
        for (int i = 0, il = aArr.size(); i < il; i++) {
            if (i > 0)
                sbStr.append(sSep);
            sbStr.append(aArr.get(i));
        }
        return sbStr.toString();
    }

    private String crawlDetailUrl(Document document, int index) {
        List<String> DetailList = new ArrayList<String>();
        DetailList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a");
        DetailList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a");
        for (String detail : DetailList) {
            String detailPath = "#result_" + Integer.toString(index) + detail;
            Element detailUrlEle = document.select(detailPath).first();
            if (detailUrlEle != null) {
                if (detailUrlEle.text().contains("https://www.amazon.com/")) {
                    String detailUrl = detailUrlEle.attr("href");
                    return detailUrl;
                }
                else {
                    return "https://www.amazon.com" + detailUrlEle.attr("href");
                }
            }
        }
        return null;
    }

    private String crawlTitle(Document document, int index) {
        String titleString = new String();
        List<String> titleList = new ArrayList<String>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2");
        try {
            for (String title : titleList) {
                //String titlePath = "#result_" + Integer.toString(index) + title;
                Element titleEle = document.select("#result_" + Integer.toString(index) + title).first();
                if (titleEle != null) {
                    //System.out.println("titleEle: " + titleEle.text());
                    titleString = titleEle.text();
                    break;
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, error occurred in function crawlTitle", e);
        }

        return titleString;
    }

    private String crawlCategory(Document document) {
        Element categoryEle = document.select("#leftNavContainer > ul:nth-child(2) > div > li:nth-child(1) > span > a > h4").first();
        return categoryEle.text();
    }

    private String crawlBrand(Document document, int index) {
        String brand = " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(2) > span:nth-child(2)";
        String brandPath = "#result_" + Integer.toString(index) + brand;
        Element brandString = document.select(brandPath).first();
        if (brandString != null) {
            return brandString.text();
        }
        else {
            return null;
        }
    }

    private String getCrawlerTitle(Document document, int index) {
        String titleString = new String();
        List<String> titleList = new ArrayList<String>();
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2");
        try {
            for (String title : titleList) {
                //String titlePath = "#result_" + Integer.toString(index) + title;
                Element titleEle = document.select("#result_" + Integer.toString(index) + title).first();
                if (titleEle != null) {
                    //System.out.println("titleEle: " + titleEle.text());
                    titleString = titleEle.text();
                    break;
                }

            }
        }
        catch (Exception e) {
            e.printStackTrace();
            logger.error("Sorry, error occurred in function crawlTitle", e);
        }

        return titleString;
    }

    private String getCrawlerCategory(Document document) {
        Element categoryEle = document.select("#leftNavContainer > ul:nth-child(2) > div > li:nth-child(1) > span > a > h4").first();
        return categoryEle.text();
    }


    private int getResultIndex(Element element) {
        String adId = element.attr("data-asin");
        //System.out.println("adId: " + adId);
        String idString = element.attr("id");
        //System.out.println("resultId : " + idString);
        if (idString == null || idString.isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(idString.substring(idString.indexOf("_") + 1));
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            logger.error("ERROR", e);
            return -1;
        }
    }

}
