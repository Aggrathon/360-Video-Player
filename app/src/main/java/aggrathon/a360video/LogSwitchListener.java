package aggrathon.a360video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import java.io.File;

class LogSwitchListener implements OnCheckedChangeListener {

	MainActivity activity;

	public  LogSwitchListener(MainActivity act) {
		activity = act;
	}

	@Override
	public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
		if(isChecked) {
			if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, MainActivity.PERMISSION_WRITE);
				activity.logSwitch.setChecked(false);
				return;
			}
			if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				Toast.makeText(activity, "Cannot write logs (storage not accessible)", Toast.LENGTH_SHORT).show();
				activity.logSwitch.setChecked(false);
				return;
			}
			File file = new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOCUMENTS), MainActivity.DIRECTORY_NAME);
			if (!file.mkdirs() && !file.isDirectory()) {
				Toast.makeText(activity, "Cannot create folder for logs", Toast.LENGTH_SHORT).show();
				activity.logSwitch.setChecked(false);
				return;
			}
		}
	}
}
