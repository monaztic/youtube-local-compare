package src;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        String pwd = ""; // present working directory
        String arg; // argument
        int i = 0;
        HashMap<String, String> videos = new HashMap<>(); // video title and id
        ArrayList<String> metadataName = new ArrayList<>(); // metadata filename
        boolean noOutput = false;
        boolean noLogging = false;

        // Parse command line arguments
        // Determine how many flags and index of argument
        for (; i < args.length && args[i].startsWith("-"); i++) {
            arg = args[i];

            if (arg.equals("-nooutput")) { noOutput = true; }
            else if (arg.equals("-nologging")) { noLogging = true; }
        }

        if (i == args.length) {
            System.out.println("Usage: youtube-local-compare [-nooutput] [-nologging] [directory]");
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

        Input input = new Input();
        Output output = new Output();

        input.findVideos(videos, pwd);
        int allVideos = videos.size(); // number of videos before pruning
        if (videos.size() == 0) { System.out.println("There were no videos found in this directory."); }
        else { System.out.println(videos.size() + " videos found."); }

        videos = input.findMetadata(videos, metadataName, pwd);
        System.out.println(metadataName.size() + "/" + allVideos +
                " videos have a corresponding .info.json file. (" + (((double)metadataName.size() / allVideos) * 100) + "%)");

        input.parseID(videos, metadataName, pwd);

        HashMap<String, String> video_status = new HashMap<>();
        String status = "";

        // Print output on the status of each video
        for (Map.Entry<String, String> entry : videos.entrySet()) {
            status = input.videoOnlineStatus(entry.getValue());
            switch (status) {
                case "available":
                    System.out.println(entry.getKey() + " is available on YouTube. https://youtube.com/watch?v=" +
                            entry.getValue());
                    break;
                case "unavailable":
                    System.out.println(entry.getKey() + " is no longer on YouTube.");
                    break;
                case "unlisted":
                    System.out.println(entry.getKey() + " is available on YouTube, but unlisted. https://youtube.com" +
                             "/watch?v=" + entry.getValue());
                    break;
                case "private":
                    System.out.println(entry.getKey() + " has been made private.");
                    break;
                case "terminated":
                    System.out.println(entry.getKey() + " is no longer available as the YouTube account which uploaded" +
                            " this video has been terminated.");
            }
            video_status.put(entry.getKey(), status);
        }

        output.printStatistics(video_status);
    }
}

