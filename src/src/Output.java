package src;

import java.util.HashMap;
import java.util.Map;

public class Output {
    //
    public void printStatistics(HashMap<String, String> video_status) {
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
}
