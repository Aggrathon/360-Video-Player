package aggrathon.a360video;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Switch;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int VIDEO_SELECTED = 1;
	public static final String VIDEO_URI = "video_uri";
	static final String VIDEO_DELAY = "video_delay";
	static final String VIDEO_LOOP = "video_loop";

    VrVideoView vrVideo;
	Switch delaySwitch;
	Switch loopSwitch;
	Uri videoUri;
	ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		delaySwitch = (Switch) findViewById(R.id.switchDelay);
		loopSwitch = (Switch) findViewById(R.id.switchLoop);
		scrollView = (ScrollView)findViewById(R.id.activity_main);
        vrVideo = (VrVideoView) findViewById(R.id.vrVideo);
		vrVideo.setEventListener(new VrVideoEventListener() {
			@Override
			public void onClick() {
				vrVideo.seekTo(0);
				vrVideo.playVideo();
			}

			@Override
			public void onCompletion() {
				if(loopSwitch.isChecked())
					onClick();
			}

			@Override
			public void onLoadSuccess() {
				if(vrVideo.getDisplayMode() == VrWidgetView.DisplayMode.EMBEDDED)
					vrVideo.pauseVideo();
			}
		});
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
        if(delaySwitch.isChecked()) {
			vrVideo.seekTo(0);
			vrVideo.pauseVideo();
			vrVideo.postDelayed(new Runnable() {
                @Override
                public void run() {
                    vrVideo.playVideo();
                }
            },5000);
        }
        else {
            vrVideo.seekTo(0);
            vrVideo.playVideo();
        }
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
			else
				vrVideo.loadVideoFromAsset("black.mp4", new VrVideoView.Options());
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e("MainActivity", "Could not open file");
			try {
				videoUri = null;
				vrVideo.loadVideoFromAsset("black.mp4", new VrVideoView.Options());
			}
			catch (IOException e2) {}

			return;
		}
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
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		String uri = savedInstanceState.getString(VIDEO_URI, null);
		if(uri != null)
			videoUri = Uri.parse(uri);
		delaySwitch.setChecked(savedInstanceState.getBoolean(VIDEO_DELAY, true));
		loopSwitch.setChecked(savedInstanceState.getBoolean(VIDEO_LOOP, true));
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
