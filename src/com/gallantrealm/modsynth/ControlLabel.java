package com.gallantrealm.modsynth;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ControlLabel extends TextView {

	int control;

	public ControlLabel(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ControlLabel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ControlLabel);
		control = a.getInt(R.styleable.ControlLabel_control, 0);
		a.recycle();
	}

	public ControlLabel(Context context) {
		super(context);
		init();
	}

	private void init() {
		setLongClickable(true);
		setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				System.out.println("LONG CLICK");
				return true;
			}
		});
	}

}
