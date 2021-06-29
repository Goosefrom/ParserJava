package parser;

//jsoup is an open-source library for html pages parsing
//import org.json.simple.JSONObject;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    private static JSONObject jsonObj = new JSONObject();

    public static void main(String[] args) throws IOException, InterruptedException {

        ArrayList<String> productNames = new ArrayList<>();
        ArrayList<String> brands = new ArrayList<>();
        ArrayList<String> colors = new ArrayList<>();
        ArrayList<String> prices = new ArrayList<>();
        ArrayList<String> articleIDs = new ArrayList<>();

        //searching number of pages
        int countOfRequests = 0;

        //get request for each page in this category
        Document doc = Jsoup.connect("https://www.aboutyou.de/c/maenner/bekleidung-20290")
                .userAgent("Chrome/4.0.249.0 Safari/532.5")
                .referrer("https://www.aboutyou.de/c/maenner/bekleidung-20290")
                .get();
        countOfRequests++;

        int numberOfPages = Integer.parseInt(doc.select("#app > section > section > div.sc-19tq43e-1.cYbXBI > div.dt8ho3-0.ykJro > ul > li:nth-child(6) > a").text());

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

            doc = Jsoup.connect("https://www.aboutyou.de" + href)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("https://www.aboutyou.de/c/maenner/bekleidung-20290")
                    .get();

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

        }


        HTTPParserThread thr1;
        HTTPParserThread thr2;
        HTTPParserThread thr3;

            for (int page = 1; page <= numberOfPages; page += 3) {


                //threads  for each product on page
                thr1 = new HTTPParserThread("https://www.aboutyou.de/c/maenner/bekleidung-20290?page=" + page + "&sort=topseller", "https://www.aboutyou.de/c/maenner/bekleidung-20290");
                thr2 = new HTTPParserThread("https://www.aboutyou.de/c/maenner/bekleidung-20290?page=" + (page + 1) + "&sort=topseller", "https://www.aboutyou.de/c/maenner/bekleidung-20290");
                thr3 = new HTTPParserThread("https://www.aboutyou.de/c/maenner/bekleidung-20290?page=" + (page + 2) + "&sort=topseller", "https://www.aboutyou.de/c/maenner/bekleidung-20290");

                thr1.start();
                thr1.join();
                thr2.start();
                thr2.join();
                thr3.start();
                thr3.join();

                //take properties from threads
                productNames.addAll(thr1.getProductNames());
                productNames.addAll(thr2.getProductNames());
                productNames.addAll(thr3.getProductNames());

                brands.addAll(thr1.getBrands());
                brands.addAll(thr2.getBrands());
                brands.addAll(thr3.getBrands());

                colors.addAll(thr1.getColors());
                colors.addAll(thr2.getColors());
                colors.addAll(thr3.getColors());

                prices.addAll(thr1.getPrices());
                prices.addAll(thr2.getPrices());
                prices.addAll(thr3.getPrices());

                articleIDs.addAll(thr1.getArticleIDs());
                articleIDs.addAll(thr2.getArticleIDs());
                articleIDs.addAll(thr3.getArticleIDs());

                countOfRequests += thr1.getCountOfRequests();
                countOfRequests += thr2.getCountOfRequests();
                countOfRequests += thr3.getCountOfRequests();

            }

            JSONObject jsonObject = new JSONObject();

            for (int i = 0; i < productNames.size(); i++) {
                jsonObject.put("product name", productNames.get(i));
                jsonObject.put("brand", brands.get(i));
                jsonObject.put("color", colors.get(i));
                jsonObject.put("price", prices.get(i));
                jsonObject.put("article ID", articleIDs.get(i));
                jsonObj.put("product", jsonObject);
            }
            System.out.println("Count of Requests: " + countOfRequests);
            System.out.println("Number of products: " + productNames.size());
        }

    }