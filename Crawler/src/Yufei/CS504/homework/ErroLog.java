package Yufei.CS504.homework;

import java.io.*;


public class ErroLog {

    public static void LogInfo(String logFilePath) {
        try {
            File file = new File(logFilePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
            BufferedWriter logWriter = new BufferedWriter(fileWriter);
        }
        catch (IOException e) {
            e.printStackTrace();
            errInfo(e);
        }
    }

    //log exception
    public static String errInfo(Exception e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            //output the error info into printWriter
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }
}
