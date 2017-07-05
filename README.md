# AmazonProductCrawler
A Crawler to get product information from Amazon

## 1. Download code
````
git clone git@github.com:GaloisGun/AmazonProductCrawler.git
````

## 2. Run

> Change input path and output path in the main method for your own

````
cd out/artifacts/AmazonProductCrawler_jar
java -jar AmazonProductCrawler.jar
````

## 3. Sample result
````
{
"adId":1,
"campaignId":8040,
"keyWords":[
        "rainbow",
        "light",
        "prenatal",
        "one",
        "multivitamin",
        "150",
        "count",
        "bottle"
        ],
"relevanceScore":0.0,
"pClick":0.0,"bidPrice":3.4,
"rankScore":0.0,
"qualityScore":0.0,
"costPerClick":0.0,
"position":0,
"title":"Rainbow Light Prenatal One Multivitamin, 150-Count Bottle",
"price":39.89,
"thumbnail":"https://images-na.ssl-images-amazon.com/images/I/41SOI2UHRrL._AC_US218_.jpg",
"description":null,
"brand":"Rainbow Light",
"detail_url":"https://www.amazon.com/gp/slredirect/picassoRedirect.html/ref=pa_sp_atf_aps_sr_pg1_2?ie=UTF8&adId=A07789501X8PNULK45LD3&url=https%3A%2F%2Fwww.amazon.com%2FRainbow-Light-Prenatal-Multivitamin-150-Count%2Fdp%2FB00115BJ30%2Fref%3Dsr_1_2%2F138-8224618-1303902%3Fie%3DUTF8%26qid%3D1499223412%26sr%3D8-2-spons%26keywords%3DPrenatal%26psc%3D1%26smid%3DA2G7B63FOSFZJZ&qualifier=1499223412&id=4746771680489145&widgetName=sp_atf",
"query":"Prenatal DHA",
"query_group_id":10,
"category":"Health & Household"
}

````
