package com.blautic.pikkucam.video;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.blautic.pikkucam.cfg.CfgDef;

import java.io.IOException;
import java.util.Calendar;


/**
 * Created by jsf on 8/10/17.
 */

public class Logo2Video {

    String TAG = "Logo2Video";
    String videoFileIn = null;
    String videoFileOut = null;
    String videoSubtitle = null;
    String videoSubtitleStart = null;
    boolean subtitleEnabled = false;
    boolean advertEnabled = false;
    String advertFile = null;
    int quality;
    int highspeedFPS = 0;
    int rateSlowMotion;
    String logo = null;
    int v_width = 0;
    int v_height = 0;
    int ratio_w = 0;
    int ratio_h = 0;
    private FFmpegKit ffmpeg;
    private Context _context;
    private Logo2VideoListener listener;
    private String settingsName;

    public Logo2Video(Context _context) {
        this._context = _context;
        listener = null;

    }

    // Assign the listener implementing events interface that will receive the events
    public void setListener(Logo2VideoListener listener) {
        this.listener = listener;
    }

    private int gcd(int a, int b) {
        while (b > 0) {
            int temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    public void setProfileSettings(String profileSettings) {
        settingsName = profileSettings;
    }

    public boolean setVideoFile(String in, String out, int q, int highspeedFPS) throws IOException {
        int mcd = 0;
        videoFileIn = in;
        videoFileOut = out;
        quality = q;
        this.highspeedFPS = highspeedFPS;
        rateSlowMotion = highspeedFPS / 30;
        Log.d(TAG, "Video para logo: " + in);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoFileIn);
            //v_width = 720;
            //v_height = 1280;
            v_width = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            v_height = Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));

            mcd = gcd(v_width, v_height);

            ratio_w = v_width / mcd;
            ratio_h = v_height / mcd;

            Log.d(TAG, "LogoVideo-- w:" + v_width + " h:" + v_height + " ratio:" + ratio_w + "/" + ratio_h);
            retriever.release();
            return true;
        } catch (IllegalArgumentException | IOException e) {
            if (listener != null) {
                listener.onFailure();
            }
            retriever.release();
            return false;
        }
    }

    public void setSubtitle(boolean subtitleOn, String txt, String start) {
        subtitleEnabled = subtitleOn;

        videoSubtitle = txt;
        videoSubtitleStart = start;
    }

    public void setAdvertInfo(boolean enable, String advFile) {
        advertEnabled = enable;
        advertFile = advFile;
    }

    public void setLogo(String l) {
        logo = l;
    }

    public void execute() {
        Calendar calendar = Calendar.getInstance();
        String date = "";

        if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
            date = date + "0" + calendar.get(Calendar.DAY_OF_MONTH) + "/";
        } else {
            date = date + calendar.get(Calendar.DAY_OF_MONTH) + "/";
        }

        if (calendar.get(Calendar.MONTH) < 10) {
            date = date + "0" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + "  ";
        } else {
            date = date + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + "  ";
        }

        if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
            date = date + "0" + calendar.get(Calendar.HOUR_OF_DAY) + ":";
        } else {
            date = date + calendar.get(Calendar.HOUR_OF_DAY) + ":";
        }

        if (calendar.get(Calendar.MINUTE) < 10) {
            date = date + "0" + calendar.get(Calendar.MINUTE) + ":";
        } else {
            date = date + calendar.get(Calendar.MINUTE) + ":";
        }

        if (calendar.get(Calendar.SECOND) < 10) {
            date = date + "0" + calendar.get(Calendar.SECOND);
        } else {
            date = date + calendar.get(Calendar.SECOND);
        }

        if (highspeedFPS > 0) {

            String overlay;

            if (subtitleEnabled && videoSubtitle != null && videoSubtitleStart != null)
                overlay = "[0:v]setpts=" + rateSlowMotion + "*PTS,drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)[v]";
            else overlay = "[0:v]setpts=" + rateSlowMotion + "*PTS,overlay=(W-w-10):(H-h-10)[v]";
            String[] complexCommand;
            switch (quality) {
                case CfgDef.VAL_VIDEOQUALITY_LOW:

                    complexCommand = new String[]{"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "copyright=pikku", "-metadata", "title=" + videoSubtitle, "-metadata", "genre=1", "-metadata", "album=" + date, "-metadata", "artist=" + settingsName, "-metadata", "track=0", "-map", "[v]", "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", videoFileOut};
                    break;

                case CfgDef.VAL_VIDEOQUALITY_MED:

                    complexCommand = new String[]{"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "copyright=pikku", "-metadata", "title=" + videoSubtitle, "-metadata", "genre=1", "-metadata", "album=" + date, "-metadata", "artist=" + settingsName, "-metadata", "track=1", "-map", "[v]", "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", videoFileOut};
                    break;

                case CfgDef.VAL_VIDEOQUALITY_HIGH:

                    complexCommand = new String[]{"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "copyright=pikku", "-metadata", "title=" + videoSubtitle, "-metadata", "genre=1", "-metadata", "album=" + date, "-metadata", "artist=" + settingsName, "-metadata", "track=2", "-map", "[v]", "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", videoFileOut};
                    break;

                default:

                    complexCommand = new String[]{"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "copyright=pikku", "-metadata", "title=" + videoSubtitle, "-metadata", "genre=1", "-metadata", "album=" + date, "-metadata", "artist=" + settingsName, "-metadata", "track=1", "-map", "[v]", "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", videoFileOut};
                    break;
            }
            execFFmpegBinary(complexCommand);

        } else {

            String overlay;

            switch (quality) {

                case CfgDef.VAL_VIDEOQUALITY_LOW:

                    if (subtitleEnabled && videoSubtitle != null && videoSubtitleStart != null) {
                        if (advertEnabled && advertFile != null) {
                            String picture = advertFile;
                            overlay = "[1:v]scale=" + v_width + "x" + v_height + ",setdar=" + ratio_w + "/" + ratio_h + ",fade=t=out:st=2:d=1[v1];[0:v]fade=t=in:st=0:d=1[v0];[v1][3:a][v0][0:a]concat=n=2:v=1:a=1[ov1][a1];[ov1]drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)[v]";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-loop", "1", "-t", "3", "-i", picture, "-i", logo, "-f", "lavfi", "-t", "0.1", "-i", "anullsrc", "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "artist=" + settingsName, "-metadata", "track=0", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "1000000", "-map", "[a1]", "-map", "[v]", videoFileOut};
                            execFFmpegBinary(complexCommand);

                        } else {
                            overlay = "drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "track=0", "-metadata", "copyright=pikku", "-metadata", "artist=" + settingsName, "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "1000000", "-c:a", "copy", videoFileOut};
                            execFFmpegBinary(complexCommand);

                        }

                    } else {
                        if (advertEnabled && advertFile != null) {
                            String picture = advertFile;
                            overlay = "[1:v]scale=" + v_width + "x" + v_height + ",setdar=" + ratio_w + "/" + ratio_h + ",fade=t=out:st=2:d=1[v1];[0:v][2:v]overlay=(W-w-5):(H-h-5)[v0];[v0]fade=t=in:st=0:d=1[v01];[v1][3:a][v01][0:a]concat=n=2:v=1:a=1[v][a1]";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-loop", "1", "-t", "3", "-i", picture, "-i", logo, "-f", "lavfi", "-t", "0.1", "-i", "anullsrc", "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "artist=" + settingsName, "-metadata", "genre=0", "-metadata", "track=0", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "1000000", "-map", "[a1]", "-map", "[v]", videoFileOut};
                            execFFmpegBinary(complexCommand);

                        } else {
                            overlay = "overlay=(W-w-5):(H-h-5)";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "track=0", "-metadata", "artist=" + settingsName, "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "1000000", "-c:a", "copy", videoFileOut};
                            execFFmpegBinary(complexCommand);
                        }
                    }


                    break;

                case CfgDef.VAL_VIDEOQUALITY_MED:

                    if (subtitleEnabled && videoSubtitle != null && videoSubtitleStart != null) {
                        if (advertEnabled && advertFile != null) {
                            String picture = advertFile;
                            overlay = "[1:v]scale=" + v_width + "x" + v_height + ",setdar=" + ratio_w + "/" + ratio_h + ",fade=t=out:st=2:d=1[v1];[0:v]fade=t=in:st=0:d=1[v0];[v1][3:a][v0][0:a]concat=n=2:v=1:a=1[ov1][a1];[ov1]drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)[v]";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-loop", "1", "-t", "3", "-i", picture, "-i", logo, "-f", "lavfi", "-t", "0.1", "-i", "anullsrc", "-filter_complex", overlay, "-metadata", "artist=" + settingsName, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "track=1", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "2500000", "-map", "[a1]", "-map", "[v]", videoFileOut};
                            execFFmpegBinary(complexCommand);

                        } else {
                            overlay = "drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)";
                            String[] complexCommand_m = {"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "artist=" + settingsName, "-metadata", "track=1", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "2500000", "-c:a", "copy", videoFileOut};
                            execFFmpegBinary(complexCommand_m);

                        }

                    } else {
                        if (advertEnabled && advertFile != null) {
                            String picture = advertFile;
                            overlay = "[1:v]scale=" + v_width + "x" + v_height + ",setdar=" + ratio_w + "/" + ratio_h + ",fade=t=out:st=2:d=1[v1];[0:v][2:v]overlay=(W-w-5):(H-h-5)[v0];[v0]fade=t=in:st=0:d=1[v01];[v1][3:a][v01][0:a]concat=n=2:v=1:a=1[v][a1]";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-loop", "1", "-t", "3", "-i", picture, "-i", logo, "-f", "lavfi", "-t", "0.1", "-i", "anullsrc", "-filter_complex", overlay, "-metadata", "artist=" + settingsName, "-metadata", "genre=0", "-metadata", "track=1", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "2500000", "-map", "[a1]", "-map", "[v]", videoFileOut};

                            execFFmpegBinary(complexCommand);

                        } else {
                            overlay = "overlay=(W-w-8):(H-h-8)";
                            String[] complexCommand_m = {"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "genre=0", "-metadata", "track=1", "-metadata", "artist=" + settingsName, "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "2500000", "-c:a", "copy", videoFileOut};
                            execFFmpegBinary(complexCommand_m);
                        }
                    }

                    break;
                case CfgDef.VAL_VIDEOQUALITY_MAX:
                case CfgDef.VAL_VIDEOQUALITY_HIGH:

                    if (subtitleEnabled && videoSubtitle != null && videoSubtitleStart != null) {
                        if (advertEnabled && advertFile != null) {
                            String picture = advertFile;

                            overlay = "[1:v]scale=" + v_width + "x" + v_height + ",setdar=" + ratio_w + "/" + ratio_h + ",fade=t=out:st=2:d=1[v1];[0:v]fade=t=in:st=0:d=1[v0];[v1][3:a][v0][0:a]concat=n=2:v=1:a=1[ov1][a1];[ov1]drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)[v]";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-loop", "1", "-t", "3", "-i", picture, "-i", logo, "-f", "lavfi", "-t", "0.1", "-i", "anullsrc", "-filter_complex", overlay, "-metadata", "artist=" + settingsName, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "track=2", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", "-map", "[a1]", "-map", "[v]", videoFileOut};

                            execFFmpegBinary(complexCommand);

                        } else {
                            overlay = "drawbox=y=ih-60:color=black@0.4:width=iw:height=40:t=fill,drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf:fontcolor=white: fontsize=18:text='" + videoSubtitleStart + "':x=10:y=(H-48),drawtext=fontfile=/system/fonts/DroidSans.ttf: fontcolor=white: fontsize=22: text='" + videoSubtitle + "': x=(W-text_w)/2:y=(H-50),drawtext=fontfile=/system/fonts/DroidSans-Bold.ttf: fontcolor=white: fontsize=18: text='PikkuCam':x=W-text_w-10:y=(H-48)";
                            String[] complexCommand_r = {"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "artist=" + settingsName, "-metadata", "track=2", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", "-c:a", "copy", videoFileOut};
                            execFFmpegBinary(complexCommand_r);

                        }

                    } else {
                        if (advertEnabled && advertFile != null) {
                            String picture = advertFile;
                            overlay = "[1:v]scale=" + v_width + "x" + v_height + ",setdar=" + ratio_w + "/" + ratio_h + ",fade=t=out:st=2:d=1[v1];[0:v][2:v]overlay=(W-w-5):(H-h-5)[v0];[v0]fade=t=in:st=0:d=1[v01];[v1][3:a][v01][0:a]concat=n=2:v=1:a=1[v][a1]";
                            String[] complexCommand = {"-y", "-i", videoFileIn, "-loop", "1", "-t", "3", "-i", picture, "-i", logo, "-f", "lavfi", "-t", "0.1", "-i", "anullsrc", "-filter_complex", overlay, "-metadata", "artist=" + settingsName, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "track=2", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", "-map", "[a1]", "-map", "[v]", videoFileOut};

                            execFFmpegBinary(complexCommand);

                        } else {
                            overlay = "overlay=(W-w-10):(H-h-10)";
                            String[] complexCommand_r = {"-y", "-i", videoFileIn, "-i", logo, "-filter_complex", overlay, "-metadata", "title=" + videoSubtitle, "-metadata", "genre=0", "-metadata", "artist=" + settingsName, "-metadata", "track=2", "-metadata", "copyright=pikku", "-metadata", "album=" + date, "-vcodec", "mpeg4", "-r", "30", "-b:v", "10000000", "-c:a", "copy", videoFileOut};
                            execFFmpegBinary(complexCommand_r);
                        }
                    }

                    break;

            }
        }

    }

    private void execFFmpegBinary(final String[] command) {
        if (listener != null) listener.onStart();
        //    try {
        FFmpegSession session = FFmpegKit.execute(command);

        if (ReturnCode.isSuccess(session.getReturnCode())) {
            /*if (highspeedFPS>0){
                addLogo();
            }*/
            Log.i("ffmpeg", "Command execution completed successfully.");
            if (listener != null) listener.onSuccess();
        } else if (ReturnCode.isCancel(session.getReturnCode())) {
            Log.i("ffmpeg", "Command execution cancelled by user.");
        } else {
            Log.i("ffmpeg", String.format("Command execution failed with state %s and rc %s.%s", session.getState(), session.getReturnCode(), session.getFailStackTrace()));
            if (listener != null) listener.onFailure();
            // Config.printLastCommandOutput(Log.INFO);
        }

      /*  } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }*/
    }

    public void mosaic(int num_inputs, String input0, String input1, String input2, String input3, String output) {

        switch (num_inputs) {
            case 1:

                break;

            case 2:
                String[] complexCommand_r_2 = {"-y", "-i", input0, "-i", input1, "-filter_complex", "[0:v] scale=800x600 [ul];[1:v] scale=800x600 [ur];[ul][ur] hstack [v]", "-vcodec", "mpeg4", "-map", "[v]", output};
                execFFmpegBinary(complexCommand_r_2);
                break;

            case 3:
                String[] complexCommand_r_3 = {"-y", "-i", input0, "-i", input1, "-i", input2, "-filter_complex", "[0:v] scale=640x480 [ul];[1:v] scale=640x480 [ur];[2:v] scale=640x480 [dl];[2:v] scale=640x480 [dr];[ul][ur] hstack [top];[dl][dr] hstack [bottom];[top][bottom]vstack[v]", "-vcodec", "mpeg4", "-map", "[v]", output};
                execFFmpegBinary(complexCommand_r_3);
                break;
            case 4:
                String[] complexCommand_r_4 = {"-y", "-i", input0, "-i", input1, "-i", input2, "-i", input3, "-filter_complex", "[0:v] scale=640x480 [ul];[1:v] scale=640x480 [ur];[2:v] scale=640x480 [dl];[3:v] scale=640x480 [dr];[ul][ur] hstack [top];[dl][dr] hstack [bottom];[top][bottom]vstack[v]", "-vcodec", "mpeg4", "-map", "[v]", output};
                execFFmpegBinary(complexCommand_r_4);
                break;
        }

    }

    public interface Logo2VideoListener {

        public void onStart();

        public void onFinish();

        public void onFailure();

        public void onSuccess();
    }

}



/*
 E/ffmpeg-kit: [mpeg4 @ 0x7e15391000] timebase 1/1000000 not supported by MPEG 4 standard, the maximum admitted value for the timebase denominator is 65535
 E/ffmpeg-kit: Error initializing output stream 0:1 -- Error while opening encoder for output stream #0:1 - maybe incorrect parameters such as bit_rate, rate, width or height
*/