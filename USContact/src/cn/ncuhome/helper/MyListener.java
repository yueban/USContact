package cn.ncuhome.helper;

public class MyListener {
	OnServiceListener onServiceListener;

	public interface OnServiceListener {
		void ServiceListening(final String downloadUrl, final String filename);
	}

	public void setOnServiceListener(OnServiceListener onServiceListener) {
		this.onServiceListener = onServiceListener;
	}
}
