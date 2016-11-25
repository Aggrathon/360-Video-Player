package aggrathon.a360video;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.google.vr.sdk.widgets.common.VrWidgetView;
import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int VIDEO_SELECTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        VrVideoView vr = (VrVideoView) findViewById(R.id.vrVideo);
        vr.setVisibility(View.INVISIBLE);
    }

    public void onSelectVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_SELECTED);
    }

    public void onCardboard(View view) {
        final VrVideoView vr = (VrVideoView) view.findViewById(R.id.vrVideo);
        if(!vr.isShown())
            return;
        Switch delay = (Switch) view.findViewById(R.id.switchDelay);
        vr.setDisplayMode(VrWidgetView.DisplayMode.FULLSCREEN_STEREO);
        if(delay.isChecked()) {
            Handler hand = new Handler();
            hand.postDelayed(new Runnable() {
                @Override
                public void run() {
                    vr.seekTo(0);
                    vr.playVideo();
                }
            },5000);
        }
        else {
            vr.seekTo(0);
            vr.playVideo();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIDEO_SELECTED && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                VrVideoView vr = (VrVideoView) findViewById(R.id.vrVideo);
                vr.setVisibility(View.VISIBLE);
                VrVideoView.Options opt = new VrVideoView.Options();
                opt.inputFormat = VrVideoView.Options.FORMAT_DEFAULT;
                opt.inputType = VrVideoView.Options.TYPE_MONO;
                vr.loadVideo(uri, opt);
                vr.pauseVideo();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "File not found");
                return;
            }
            catch (IOException e) {
                e.printStackTrace();
                Log.e("MainActivity", "Could not open file");
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VrVideoView vr = (VrVideoView) findViewById(R.id.vrVideo);
        vr.pauseRendering();
        vr.shutdown();
    }
}
