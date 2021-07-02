package com.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class HTTPParserThread extends Thread{
    private String url;
    private String referrer;
    private Document doc;
    private int countOfRequests;

    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayList<String> brands = new ArrayList<>();
    private ArrayList<String> colors = new ArrayList<>();
    private ArrayList<String> prices = new ArrayList<>();
    private ArrayList<String> articleIDs = new ArrayList<>();

    public HTTPParserThread(String url, String referrer){
        this.url = url;
        this.referrer = referrer;
    }

    @Override
    public void run(){
        //each page
        try {
            doc = Jsoup.connect(url)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer(referrer)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!doc.select("#app > section > section > div.sc-19tq43e-1.cYbXBI > div.dt8ho3-0.ykJro > ul > li.pageNumbers.active > a").text().equals("1")) {
            //refs for each clothing on one page
            ArrayList<String> hrefs = new ArrayList<>();
            Elements rows = doc.select("div.sc-1n50fuf-0.UIpOS");
            for (Element row : rows) {
                Elements refs = row.select("a");
                for (Element ref : refs) {
                    hrefs.add(ref.attr("href"));
                }
            }

            for (String href : hrefs) {
                //get html for clothing page
                try {
                    doc = Jsoup.connect("https://www.aboutyou.de" + href)
                            .userAgent("Chrome/4.0.249.0 Safari/532.5")
                            .referrer(url)
                            .get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                countOfRequests++;

                //product name
                productNames.add(doc.select("div.iay39c-1.dZjUXd").text());

                //brand
                brands.add(doc.select("img.iay39c-0.dtCJGg").attr("alt"));

                //color
                colors.add(doc.select("span.jlvxcb-1.KCIhX").text());

                //price
                prices.add(doc.select("span.sc-1kqkfaq-0.x3voc9-0.hztKGd.kOcfQo").text());

                //article ID
                articleIDs.add(doc.select("div.d5kk8t-9.RIujM:nth-child(1) > p.d5kk8t-7.gEmLW").text());

                //for all color/size combinations need to get html for each color of a product and search available sizes
                // with id == SizeBubble_available/disable

            }
        }
    }

    public int getCountOfRequests(){
        return countOfRequests;
    }

    public ArrayList<String> getProductNames() { return productNames; }

    public ArrayList<String> getBrands() { return brands; }

    public ArrayList<String> getColors() { return colors; }

    public ArrayList<String> getPrices() { return prices; }

    public ArrayList<String> getArticleIDs() { return articleIDs; }
}
