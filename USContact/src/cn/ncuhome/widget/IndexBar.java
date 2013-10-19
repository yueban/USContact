package cn.ncuhome.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import cn.ncuhome.uscontact.R;

public class IndexBar extends View {

	private ListView listView;
	private char[] c;
	private RelativeLayout indexDialog;
	private TextView textViewDialog;
	private SectionIndexer sectionIndexer = null;

	public IndexBar(Context context) {
		super(context);
		init();
	}

	public IndexBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public IndexBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		c = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
	}

	public void setRelativeLayoutDialog(RelativeLayout indexDialog) {
		this.indexDialog = indexDialog;
		this.textViewDialog = (TextView) indexDialog.findViewById(R.id.textViewDialog);
	}

	public void setListView(ListView listView) {
		this.listView = listView;
		sectionIndexer = (SectionIndexer) listView.getAdapter();
	}

	Paint paint = new Paint();

	@Override
	protected void onDraw(Canvas canvas) {
		paint.setColor(0xFFA6A9AA);
		paint.setTextSize(getMeasuredHeight() / 35);
		paint.setTextAlign(Paint.Align.CENTER);
		float widthCenter = getMeasuredWidth() / 2;
		if (c.length > 0) {
			float height = getMeasuredHeight() / c.length;
			for (int i = 0; i < c.length; i++) {
				canvas.drawText(String.valueOf(c[i]), widthCenter, (i + 1) * height, paint);
			}
		}
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int idx = ((int) event.getY()) / (getMeasuredHeight() / c.length);
		if (idx >= c.length) {
			idx = c.length - 1;
		} else if (idx < 0) {
			idx = 0;
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
			// 获取当前索引位置
			int position = sectionIndexer.getPositionForSection(c[idx]);
			if (position != -1) {
				indexDialog.setVisibility(View.VISIBLE);
				textViewDialog.setText(c[idx] + "");
				listView.setSelection(position);
			}
		} else {
			indexDialog.setVisibility(View.INVISIBLE);
		}
		return true;
	}
}
