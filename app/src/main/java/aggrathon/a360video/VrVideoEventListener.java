package aggrathon.a360video;


import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.google.vr.sdk.widgets.common.VrWidgetView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

class VrVideoEventListener extends com.google.vr.sdk.widgets.video.VrVideoEventListener {

	MainActivity activity;
	String fileName;
	BufferedWriter fileWriter;

	public  VrVideoEventListener(MainActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick() {
		activity.vrVideo.seekTo(0);
		activity.vrVideo.playVideo();
	}

	@Override
	public void onCompletion() {
		if(activity.loopSwitch.isChecked())
			onClick();
	}

	@Override
	public void onLoadSuccess() {
		if(activity.vrVideo.getDisplayMode() == VrWidgetView.DisplayMode.EMBEDDED)
			activity.vrVideo.pauseVideo();
	}

	@Override
	public void onDisplayModeChanged(int newDisplayMode) {
		if(newDisplayMode == VrWidgetView.DisplayMode.FULLSCREEN_STEREO) {
			activity.vrVideo.seekTo(0);
			if (activity.delaySwitch.isChecked()) {
				activity.vrVideo.pauseVideo();
				activity.vrVideo.postDelayed(new Runnable() {
					@Override
					public void run() {
						activity.vrVideo.playVideo();
					}
				}, 5000);
			} else {
				activity.vrVideo.playVideo();
			}
			if(activity.logSwitch.isChecked()) {
				Uri uri = Uri.parse(activity.getIntent().getStringExtra(MainActivity.VIDEO_URI));
				fileName = uri.getLastPathSegment()+"_"+DateFormat.format("yyyyMMddHHmmss",GregorianCalendar.getInstance())+".csv";
				File logFile;
				logFile = new File(Environment.getExternalStoragePublicDirectory(
						Environment.DIRECTORY_DOCUMENTS), MainActivity.DIRECTORY_NAME+File.pathSeparator+fileName);
				try {
					if(logFile.createNewFile() && logFile.canWrite()) {
						fileWriter = new BufferedWriter(new FileWriter(logFile));
						fileWriter.write("Time, Video Time, Yaw, Pitch, Roll");
						fileWriter.newLine();
					}
					else {
						fileWriter = null;
					}
				}
				catch (IOException ioe) {
					fileWriter = null;
				}
			}
		}
		else {
			if(fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException ioe) {

				}
				fileWriter = null;
				Toast.makeText(activity, "Log saved to "+fileName, Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onNewFrame() {

	}
}
