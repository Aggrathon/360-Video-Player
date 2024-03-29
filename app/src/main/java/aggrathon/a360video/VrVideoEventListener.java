package aggrathon.a360video;


import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.google.vr.sdk.widgets.common.VrWidgetView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;

class VrVideoEventListener extends com.google.vr.sdk.widgets.video.VrVideoEventListener {

	final float RAD2DEG = 180f/(float)Math.PI;
	final long LOG_INTERVAL = 200;

	MainActivity activity;

	String fileName;
	BufferedWriter fileWriter;
	long startTime;
	float[] rotations;
	Runnable logger;

	public  VrVideoEventListener(final MainActivity activity) {
		this.activity = activity;
		rotations = new float[2];
		logger = new Runnable() {
			@Override
			public void run() {
				if(fileWriter != null) {
					long currTime = System.currentTimeMillis();
					try {
						activity.vrVideo.getHeadRotation(rotations);
						fileWriter.write(((float)(currTime-startTime)/1000)+", ");
						fileWriter.write(((float)activity.vrVideo.getCurrentPosition()/1000)+", ");
						fileWriter.write(rotations[0]+", "+rotations[1]);
						fileWriter.newLine();
						activity.vrVideo.postDelayed(logger, LOG_INTERVAL);
					}
					catch (IOException ioe) {
						fileWriter = null;
						Toast.makeText(activity, "Cannot write current status to log file", Toast.LENGTH_SHORT).show();
						activity.vrVideo.removeCallbacks(logger);
					}
				}
				else {
					activity.vrVideo.removeCallbacks(logger);
				}
			}
		};
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
				String str = activity.getIntent().getStringExtra(MainActivity.VIDEO_URI);
				if(str != null) {
					fileName = getFileName(Uri.parse(str)) + "_" + DateFormat.format("yyyyMMddHHmmss", GregorianCalendar.getInstance()) + ".csv";
					try {
						File logFile = new File(Environment.getExternalStoragePublicDirectory(
								Environment.DIRECTORY_DOCUMENTS), MainActivity.DIRECTORY_NAME + File.separator + fileName);
						File dirFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), MainActivity.DIRECTORY_NAME);
						dirFile.mkdirs();
						if (logFile.createNewFile() && logFile.canWrite()) {
							fileWriter = new BufferedWriter(new FileWriter(logFile));
							fileWriter.write("Time, Video Time, Yaw, Pitch");
							fileWriter.newLine();
							startTime = System.currentTimeMillis();
							activity.vrVideo.post(logger);
						} else {
							fileWriter = null;
							Toast.makeText(activity, "Cannot create log file", Toast.LENGTH_SHORT).show();
						}
					} catch (IOException ioe) {
						fileWriter = null;
						Toast.makeText(activity, "Failed to create and write to file", Toast.LENGTH_SHORT).show();
						Log.e("File", "Cannot Create file "+fileName);
						ioe.printStackTrace();
					}
				}
				else {
					fileWriter = null;
					Toast.makeText(activity, "Default video is not loggable", Toast.LENGTH_SHORT).show();
				}
			}
		}
		else {
			if(fileWriter != null) {
				activity.vrVideo.removeCallbacks(logger);
				try {
					fileWriter.close();
				} catch (IOException ioe) {
				}
				fileWriter = null;
				Toast.makeText(activity, "Log saved to \n"+fileName, Toast.LENGTH_SHORT).show();
			}
			if(newDisplayMode == VrWidgetView.DisplayMode.EMBEDDED) {
				activity.vrVideo.pauseVideo();
			}
		}
	}

	String getFileName(Uri uri) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
			try {
				if (cursor != null && cursor.moveToFirst()) {
					result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
				}
			} finally {
				cursor.close();
			}
		}
		if (result == null) {
			result = uri.getPath();
		}
		int start = result.lastIndexOf("/");
		int end = result.lastIndexOf(".");
		if(start < 0) start = 0;
		if(end < 0) end = result.length()-1;
		return result.substring(start,end).replaceAll("\\W+", "");
	}

}
