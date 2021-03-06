package aggrathon.a360video;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int VIDEO_SELECTED = 1;
	public static final String VIDEO_URI = "video_uri";
	static final String VIDEO_DELAY = "video_delay";
	static final String VIDEO_LOOP = "video_loop";
	static final String VIDEO_LOG = "video_log";
	public static final String DIRECTORY_NAME = "360VideoPlayer";
	static final int PERMISSION_WRITE = 1;

    VrVideoView vrVideo;
	Switch delaySwitch;
	Switch loopSwitch;
	Switch logSwitch;
	Uri videoUri;
	ScrollView scrollView;

	LogSwitchListener logSwitchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		delaySwitch = (Switch) findViewById(R.id.switchDelay);
		loopSwitch = (Switch) findViewById(R.id.switchLoop);
		logSwitch = (Switch) findViewById(R.id.switchLog);
		logSwitchListener = new LogSwitchListener(this);
		logSwitchListener.SetChecked(logSwitch,	(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
		logSwitch.setOnCheckedChangeListener(logSwitchListener);
		scrollView = (ScrollView)findViewById(R.id.activity_main);
        vrVideo = (VrVideoView) findViewById(R.id.vrVideo);
		vrVideo.setEventListener(new VrVideoEventListener(this));
		playVideo(getIntent());
    }

    public void onSelectVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_SELECTED);
    }

    public void onCardboard(View view) {
        vrVideo.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);
    }

	void playVideo(Intent intent) {
		if(intent.getAction() == Intent.ACTION_VIEW) {
			String str = intent.getStringExtra(VIDEO_URI);
			if(str != null && !str.isEmpty())
				playVideo(Uri.parse(str));
		}
		playVideo(videoUri);
	}

	void playVideo(Uri uri) {
		try {
			videoUri = uri;
			if (videoUri != null) {
				vrVideo.loadVideo(videoUri, new VrVideoView.Options());
				Intent intent = getIntent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.putExtra(VIDEO_URI, videoUri.toString());
				setIntent(intent);
			}
			else {
				vrVideo.loadVideoFromAsset("black.mp4", new VrVideoView.Options());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e("MainActivity", "Could not open video file");
			Toast.makeText(this, "Could not open video file", Toast.LENGTH_SHORT).show();
			try {
				videoUri = null;
				vrVideo.loadVideoFromAsset("black.mp4", new VrVideoView.Options());
			}
			catch (IOException e2) {}

			return;
		}
		Log.d("Video", videoUri == null? "black.mp4" : videoUri.toString());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		playVideo(intent);
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIDEO_SELECTED && resultCode == RESULT_OK) {
			playVideo(data.getData());
			scrollView.post(new Runnable() {
				@Override
				public void run() {
					scrollView.fullScroll(View.FOCUS_DOWN);
				}
			});
        }
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(videoUri != null)
			outState.putString(VIDEO_URI,videoUri.toString());
		outState.putBoolean(VIDEO_DELAY, delaySwitch.isChecked());
		outState.putBoolean(VIDEO_LOOP, loopSwitch.isChecked());
		outState.putBoolean(VIDEO_LOG, logSwitch.isChecked());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		delaySwitch.setChecked(savedInstanceState.getBoolean(VIDEO_DELAY, true));
		loopSwitch.setChecked(savedInstanceState.getBoolean(VIDEO_LOOP, true));
		logSwitch.setChecked(savedInstanceState.getBoolean(VIDEO_LOG, true));

		String uri = savedInstanceState.getString(VIDEO_URI, null);
		if(uri != null)
			playVideo(Uri.parse(uri));
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if(requestCode == PERMISSION_WRITE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			logSwitchListener.SetChecked(logSwitch,true);
		}
	}

	@Override
    protected void onPause() {
        super.onPause();
        if(vrVideo != null)
            vrVideo.pauseRendering();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(vrVideo != null)
            vrVideo.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        vrVideo.shutdown();
		super.onDestroy();
    }
}
