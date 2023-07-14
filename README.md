# youtube-local-compare

### Compares your offline YouTube video collection with those online

This program, using .info.json files provided by [youtube-dl](https://github.com/ytdl-org/youtube-dl)'s `--write-info-json` command, checks the online status of video files in a directory, specifically using the unique id of every video. This can be useful for archival, as it identifies which videos of yours have been privated, removed, or are otherwise unavailable.

Currently, the program depends on a `.info.json` file with the same filename (excluding extension) of the video.

### Usage

The program is enabled by using `youtube-local-compare [flags] [directory]`. If no directory is specified, the current
directory is assumed.

The following flags are available:

`-nooutput` or `-no`: silences console output. Does not prevent logging to a file.

`-minimaloutput` or `-mo`: only displays a count of the number of videos processed in the console, rather than displaying
the status of each individual video. Logging output remains the same.

`-nologging` or `-nl`: does not log output to a file.

`-yt`: In the output, a video's YouTube title is used rather than its filename without extension. This additionally checks
whether the title at the time of download is the same as the current title. Enabling this setting takes additional
time as the title of the page must be fetched twice.