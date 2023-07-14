package src;

import org.apache.commons.io.output.TeeOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Output {
    //
    public void printStatistics(HashMap<String, String> video_status) {
        int available, unavailable, unlisted, privated;
        available = unavailable = unlisted = privated = 0;

        for (Map.Entry<String, String> video : video_status.entrySet()) {
            switch (video.getValue()) {
                case "Available": available++; break;
                case "Unavailable": unavailable++; break;
                case "Unlisted": unlisted++; break;
                case "Privated": privated++; break;
            }
        }

        if (video_status.size() == available) { System.out.println("All videos are currently available on YouTube."); }
        else if (available == 0) {
            System.out.println("None of the provided videos are currently available on YouTube.");
        }
        else {
            System.out.println("Of the " + video_status.size() + " videos with a corresponding metadata file, " +
                    available + " are currently available on YouTube. (" + (((double)video_status.size() / available) * 100) + "%)");
        }
    }

    public FileOutputStream createLoggingFile(String pwd, FileOutputStream logFile) {
        try {
            String directoryName = pwd.substring(pwd.lastIndexOf("/") + 1);
            // Log filename is of the form ylc-[unix time]-[directory name]
            File log = new File("ylc-" + Instant.now().getEpochSecond() + "-" + directoryName + ".txt");
            if (log.createNewFile()) {
                logFile = new FileOutputStream(log);
            }
            else {
                System.out.println("Unable to create logging file.");
            }
        }
        catch (IOException e) {
            System.out.println("Unable to create logging file.");
        }
        return logFile;
    }

    public void printEachVideo(HashMap<String, String> videos, HashMap<String, String> video_status, boolean noOutput,
                               boolean minimalOutput, boolean noLogging, boolean youtubeTitle, FileOutputStream logFile,
                               PrintStream console, Input input) {
        // If no output is enabled, but no logging is not enabled, redirect output to the log file only
        if (noOutput) {
            if (!noLogging) {
                System.setOut(new PrintStream(logFile));
            }
        }

        // Print output on the status of each video
        if (minimalOutput) { // this should not be printed if minimal output is enabled
            if (!noLogging) {
                System.setOut(new PrintStream(logFile));
                System.out.printf("------------------------------------------------------------------------------------------%n");
                System.setOut(new PrintStream(console));
            }
        }
        int entriesProcessed = 0;

        String status = "";
        for (Map.Entry<String, String> entry : videos.entrySet()) {
            try {
                status = input.videoOnlineStatus(entry.getValue());
            }
            catch (Exception e) { }
            if (youtubeTitle) {
                if (entry.getKey().startsWith("!")) {
                    System.out.println("The current title on YouTube and the title when downloaded differ:");
                    System.out.println("| Title when downloaded | " + entry.getKey().substring(1));
                    Document document = null;
                    try {
                        document = Jsoup.connect("https://www.youtube.com/watch?v=" + entry.getValue()).get();
                    }
                    catch (Exception e) { }
                    String videoTitle = document.title().substring(0, document.title().lastIndexOf(" - YouTube"));
                    System.out.println("| YouTube Title | " + videoTitle);
                }
                else { System.out.println("| YouTube Title | " + entry.getKey()); }
                System.out.printf("| %-11s | %-30s |%n", status, "https://youtube.com/watch?v=" + entry.getValue());
            }
            else if (minimalOutput) {
                if (!noLogging) {
                    System.setOut(new PrintStream(logFile));
                    System.out.println("| Title | " + entry.getKey());
                    System.out.printf("| %-11s | %-30s |%n", status, "https://youtube.com/watch?v=" + entry.getValue());
                    System.setOut(new PrintStream(console));
                }
                System.out.print(++entriesProcessed + "/" + videos.size() + " videos processed...\r");
            }
            else {
                System.out.println("| Title | " + entry.getKey());
                System.out.printf("| %-11s | %-30s |%n", status, "https://youtube.com/watch?v=" + entry.getValue());
            }
            video_status.put(entry.getKey(), status);
        }

        if (!noLogging) {
            System.setOut(new PrintStream(logFile));
            System.out.println("Recorded online status at " + Instant.now().getEpochSecond() + " (" +
                    new java.util.Date().toString() + ")");
        }
    }
}
