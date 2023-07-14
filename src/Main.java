import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Main {

    // Finds all videos in the current directory and updates the videos HashMap.
    public static void findVideos(HashMap<String, String> videos, String pwd) {
        File[] files = new File(pwd).listFiles();

        for (File file : files) {
            try {
                if (Files.probeContentType(file.toPath()).contains("video")) {
                    videos.put(file.getName(), null); // leave id blank for now
                }
            }
            catch (Exception e) {}
        }
    }

    // For every video found in the directory, a .info.json file with the same filename is attempted to be found.
    // A located corresponding .info.json file is added to the metadataName HashMap.
    public static HashMap<String, String> findMetadata(HashMap<String, String> videos,
                                                       ArrayList<String> metadata, String pwd) {
        HashMap<String, String> replaceVideo = new HashMap<>(); // prune videos without an .info.json file
        for (Map.Entry<String, String> video : videos.entrySet()) {
            // get the filename without the extension to compare
            String nameWithoutExtension = video.getKey().substring(0, video.getKey().lastIndexOf("."));
            File possible = new File(pwd + "/" + nameWithoutExtension + ".info.json");
            if (possible.exists()) {
                replaceVideo.put(nameWithoutExtension, null);
                metadata.add(pwd + "/" + nameWithoutExtension + ".info.json");
            }
        }
        return replaceVideo;
    }

    // Given the corresponding .info.json found, parse the JSON id and add it to its associated video title.
    public static void parseID(HashMap<String, String> videos, ArrayList<String> metadata, String pwd) {
        JSONParser parser = new JSONParser();
        for (String md : metadata) {
            try {
                Object obj = parser.parse(new FileReader(md));
                JSONObject json = (JSONObject) obj;

                String id = (String) json.get("id");
                if (id != null) { // Add the parsed id to its corresponding video title and add it to the videos HashMap
                    for (Map.Entry<String, String> video : videos.entrySet()) {
                        String nameWithoutExtension = md.substring(0, md.indexOf(".info.json"));
                        nameWithoutExtension = nameWithoutExtension.replace(pwd + "/", "");
                        if (video.getKey().equals(nameWithoutExtension)) {
                            videos.put(video.getKey(), id);
                        }
                    }
                }
            }
            catch (Exception e) {}
        }
    }

    // Determine the video status of a video with a given ID.
    public static String videoOnlineStatus(String id) throws Exception {
        URL url = new URL("https://www.youtube.com/watch?v=" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        BufferedReader read = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String line;
        String status = "available";
        boolean isPrivate = true;
        while ((line = read.readLine()) != null) {
            if (line.contains("This video isn't available")) {
                status = "unavailable";
            } else if (line.contains("\"isUnlisted\":true")) {
                status = "unlisted";
                break;
            } else if (line.contains("\"isPrivate:\":false")) {
                isPrivate = false;
            } else if (line.contains("terminated")) {
                status = "terminated";
                break;
            }
        }
        Document document = Jsoup.connect("https://www.youtube.com/watch?v=" + id).get();
        if (document.title().equals("- YouTube") && status.equals("terminated")) {
            isPrivate = false;
        }
        else if (!document.title().equals("- YouTube")) { isPrivate = false; }
        if (isPrivate) {
            status = "private";
        }
        read.close();
        return status;
    }

    //
    public static void printStatistics(HashMap<String, String> video_status) {
        int available, unavailable, unlisted, privated;
        available = unavailable = unlisted = privated = 0;

        for (Map.Entry<String, String> video : video_status.entrySet()) {
            switch (video.getValue()) {
                case "available": available++; break;
                case "unavailable": unavailable++; break;
                case "unlisted": unlisted++; break;
                case "privated": privated++; break;
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

    public static void main(String[] args) throws Exception {
        String pwd; // present working directory
        HashMap<String, String> videos = new HashMap<>(); // video title and id
        ArrayList<String> metadataName = new ArrayList<>(); // metadata filename

        if (args.length < 1) {
            System.out.println("No directory provided; using current directory.");
            pwd = System.getProperty("user.dir");
        } else {
            pwd = args[0];
            while (true) {
                if (!Files.isDirectory(Paths.get(pwd))) {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("The given input is not a directory. Please enter a correct directory:");
                    pwd = scanner.nextLine();
                }
                else { break; }
            }
        }

        findVideos(videos, pwd);
        int allVideos = videos.size(); // number of videos before pruning
        System.out.println(videos.size() + " videos found.");

        videos = findMetadata(videos, metadataName, pwd);
        System.out.println(metadataName.size() + "/" + allVideos +
                " videos have a corresponding .info.json file. (" + (((double)metadataName.size() / allVideos) * 100) + "%)");

        parseID(videos, metadataName, pwd);

        HashMap<String, String> video_status = new HashMap<>();
        String status = "";

        // Print output on the status of each video
        for (Map.Entry<String, String> entry : videos.entrySet()) {
            status = videoOnlineStatus(entry.getValue());
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

        printStatistics(video_status);
    }
}

