package Yufei.CS504.homework;


import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.List;

import static Yufei.CS504.homework.ErroLog.errInfo;

public class main {

    public static void main(String[] args) {
        //Initialize Crawler
        AmazonCrawler amazonCrawler = new AmazonCrawler();
        amazonCrawler.initProxy();

        //Initialize ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        File inputFile = new File("/Users/wuyufei/IdeaProjects/AmazonProductCrawler/Crawler/src/Yufei/CS504/homework/rawQuery3.txt");
        File ouputFile = new File("/Users/wuyufei/IdeaProjects/AmazonProductCrawler/Crawler/src/Yufei/CS504/homework/amazonProducts.json");
        BufferedReader bufferedReader = null;
        //System.out.println(inputFile.length());

        try {
            if(!ouputFile.exists())
                ouputFile.createNewFile();

            FileWriter fileWriter = new FileWriter(ouputFile.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedReader = new BufferedReader(new FileReader(inputFile));
            String line = "";
            int column = 1;
            while ((line = bufferedReader.readLine()) != null) {
                column++;

                if(line.isEmpty())
                    continue;

                String[] parts = line.split(",");
                String query = parts[0].trim();
                double bidprice = Double.parseDouble(parts[1].trim());
                int campaignId = Integer.parseInt(parts[2].trim());
                int queryGroupId = Integer.parseInt(parts[3].trim());
                List<Ad> adList = amazonCrawler.getProds(query, bidprice, campaignId, queryGroupId, 1);
                for (Ad ad : adList) {
                    String temp = objectMapper.writeValueAsString(ad);
                    bufferedWriter.write(temp);
                    bufferedWriter.newLine();
                }
                Thread.sleep(3000);
            }
            bufferedReader.close();
            bufferedWriter.close();

        } catch (EOFException e) {
            //the end of file have been reached
            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            //the specified file not exist
            e.printStackTrace();
        }
        catch (ObjectStreamException e) {
            //the file is corrupted
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            errInfo(e);
        }
        catch (IOException e) {
            //some other I/O error occurred
            e.printStackTrace();
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    errInfo(e);
                }
            }
        }


    }
}
