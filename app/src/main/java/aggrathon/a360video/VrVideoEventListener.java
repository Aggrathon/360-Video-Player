package aggrathon.a360video;


import com.google.vr.sdk.widgets.common.VrWidgetView;

class VrVideoEventListener extends com.google.vr.sdk.widgets.video.VrVideoEventListener {

	MainActivity activity;

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
		}
	}

	@Override
	public void onNewFrame() {

	}
}
