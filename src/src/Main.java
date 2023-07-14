package src;
import com.sun.tools.jconsole.JConsoleContext;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Document;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.Scanner;
import org.apache.commons.io.output.TeeOutputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        String pwd = ""; // present working directory
        String arg; // argument
        int i = 0;
        FileOutputStream logFile = null; // Stream to output to log file
        HashMap<String, String> videos = new HashMap<>(); // video title and id
        ArrayList<String> metadataName = new ArrayList<>(); // metadata filename
        boolean noOutput = false;
        boolean minimalOutput = false;
        boolean noLogging = false;
        boolean youtubeTitle = false;
        Input input = new Input();
        Output output = new Output();

        // Parse command line arguments
        // Determine how many flags and index of argument
        for (; i < args.length && args[i].startsWith("-"); i++) {
            arg = args[i];

            if (arg.equals("-no") || arg.equals("-nooutput")) { // cannot coexist
                if (minimalOutput) { minimalOutput = false; }
                noOutput = true;
            }
            else if (arg.equals("-nl") || arg.equals("-nologging")) { noLogging = true; }
            else if (arg.equals("-yt")) { // cannot coexist
                if (minimalOutput) { minimalOutput = false; }
                youtubeTitle = true;
            }
            else if (arg.equals("-mo") || arg.equals("-minimaloutput")) { // cannot coexist
                if (noOutput) { noOutput = false; }
                if (youtubeTitle) { youtubeTitle = false; }
                minimalOutput = true;
            }
        }

        // If no arguments are entered
        if (i == args.length && args.length != 0) {
            System.out.println("Usage: youtube-local-compare [-no] [-nl] [-yt] [directory]");
            System.exit(0);
        }

        if (args.length < 1) { // no arguments provided; use current directory
            System.out.println("No directory provided; using current directory.");
            pwd = System.getProperty("user.dir");
        }
        else { // one argument provided, being the directory to use
            pwd = args[i];
            while (true) {
                if (!Files.isDirectory(Paths.get(pwd))) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("The given input is not a directory. Please enter a correct directory:");
                    pwd = scanner.nextLine();
                }
                else { break; }
            }
        }

        // Create logging file
        if (!noLogging) {
            logFile = output.createLoggingFile(pwd, logFile);
        }

        // Redirect output to a dummy stream if no output is desired
        if (noOutput) {
                PrintStream dummyStream = new PrintStream(new OutputStream() {
                    public void write(int a) {
                    }
                });

                System.setOut(dummyStream);
        }

        // Locate the videos in the directory.
        input.findVideos(videos, pwd);
        int allVideos = videos.size(); // number of videos before pruning
        if (videos.size() == 0) {
            System.out.println("There were no videos found in this directory.");
            System.exit(0);
        }
        else { System.out.println(videos.size() + " videos found."); }

        // Find the corresponding .info.json files for each video.
        videos = input.findMetadata(videos, metadataName, pwd);
        System.out.println(metadataName.size() + "/" + allVideos +
                " videos have a corresponding .info.json file. (" + (((double)metadataName.size() / allVideos) * 100) + "%)");

        // Parse the id of the found metadata files.
        input.parseID(videos, metadataName, pwd, youtubeTitle);

        HashMap<String, String> video_status = new HashMap<>(); // video title and status

        if (!noOutput) {
            if (!noLogging && !minimalOutput) {
                // This allows the stream to be output at both the log file and console at once
                System.setOut(new PrintStream(new TeeOutputStream(System.out, logFile)));
            }
        }

        PrintStream console = System.out; // needed to store to reset later

        // Print status of each individual video
        output.printEachVideo(videos, video_status, noOutput, minimalOutput, noLogging, youtubeTitle, logFile, console, input);

        if (minimalOutput) {
            System.setOut(new PrintStream(new TeeOutputStream(console, logFile)));
        }
        else if (noOutput) {
            System.setOut(new PrintStream(logFile));
        }
        output.printStatistics(video_status, minimalOutput);
    }
}

