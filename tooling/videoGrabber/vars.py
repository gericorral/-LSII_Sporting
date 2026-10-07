cd "C:\Users\gerar\Desktop\UNI\4 any\lab 2\-LSII_Sporting\frontend"
npm install
npm run dev"""
Each one of the following variables holds the name of the program related to it.
For instance, FFMPEG_BIN in Windows would be ffmpeg.exe, whereas in Linux/macOS would be ffmpeg
If these programs cannot be located through your PATH environment variable, either:
 - modify PATH
 - include the absolute path in the variable content. Example for yt-dlp
Remember you can download the yt-dlp matching your system here:
https://github.com/yt-dlp/yt-dlp?tab=readme-ov-file#release-files

This has been added to the gitignore, so each team member can have an specific configuration
"""


# Use executables available through PATH on Windows, Linux, and macOS.
YT_DLP_BIN="yt-dlp"
FFMPEG_BIN="ffmpeg"
FFPROBE_BIN="ffprobe"
