# youtube-local-compare

### Compares your offline YouTube video collection with those online

This program, using .info.json files provided by [youtube-dl](https://github.com/ytdl-org/youtube-dl)'s `--write-info-json` command, checks the online status of video files in a directory, specifically using the unique id of every video. This can be useful for archival, as it identifies which videos of yours have been privated, removed, or are otherwise unavailable.

Currently, the program depends on a `.info.json` file with the same filename (excluding extension) of the video.