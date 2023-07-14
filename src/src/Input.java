package src;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Input {
    // Finds all videos in the current directory and updates the videos HashMap.
    public void findVideos(HashMap<String, String> videos, String pwd) {
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
    public HashMap<String, String> findMetadata(HashMap<String, String> videos,
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
    public void parseID(HashMap<String, String> videos, ArrayList<String> metadata, String pwd) {
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
    public String videoOnlineStatus(String id) throws Exception {
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
}
