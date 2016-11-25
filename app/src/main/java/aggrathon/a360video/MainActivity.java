package aggrathon.a360video;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    static final int VIDEO_SELECTED = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onSelectVideo(View view) {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, VIDEO_SELECTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == VIDEO_SELECTED && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            try {
                AssetFileDescriptor afd = getContentResolver().openAssetFileDescriptor(uri, "r");
                Log.i("MainActivity", "File found: "+uri.toString());
                Intent intent = new Intent(this, VideoActivity.class);
                intent.putExtra(VideoActivity.VIDEO_FILE_EXTRA, uri.toString());
                startActivity(intent);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity", "File not found");
                return;
            }
        }
    }
}
