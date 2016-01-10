package finalproject.productivityup.ui;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * Created by User on 1/11/2016.
 */
public class AlarmPlayerService extends Service {
    public static final String ALARM_PLAYER_START = "ALARM_PLAYER_START";
    public static final String ALARM_PLAYER_STOP = "ALARM_PLAYER_STOP";
    private MediaPlayer mMediaPlayer;
    private boolean isPlaying = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(this, alert);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ALARM_PLAYER_START:
                        if (isPlaying) {
                            return START_NOT_STICKY;
                        }

                        try {
                            mMediaPlayer.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return START_NOT_STICKY;
                        }
                        mMediaPlayer.start();
                        isPlaying = true;
                        break;
                    case ALARM_PLAYER_STOP:
                        mMediaPlayer.stop();
                        isPlaying = false;
                        break;
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
