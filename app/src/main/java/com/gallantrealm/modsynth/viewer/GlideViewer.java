package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Glide;
import com.gallantrealm.modsynth.module.Module;
import android.graphics.Canvas;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;

public class GlideViewer extends ModuleViewer {
	
	Glide module;

	public GlideViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Glide)module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 30, y + 25);
		path.lineTo(x - 20, y + 25);
		path.lineTo(x + 20, y - 25);
		path.lineTo(x + 30, y - 25);
		canvas.drawPath(path, paint);
	}

	@Override
	public int getViewResource() {
		return R.layout.glidepane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		SeekBar envAttack = (SeekBar) view.findViewById(R.id.glideUp);
		envAttack.setProgress((int) (100 * module.glideUp)); //Math.sqrt(module.glideUp)));
		envAttack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.glideUp = progress / 100.0f; //progress * progress / 10000.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View)envAttack.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.glideUpCC));

		SeekBar envDecay = (SeekBar) view.findViewById(R.id.glideDown);
		envDecay.setProgress((int) (100 * module.glideDown)); //Math.sqrt(module.glideDown)));
		envDecay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.glideDown = progress / 100.0f;  //* progress / 10000.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View)envDecay.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.glideDownCC));

		CheckBox glideAudioCheckBox = (CheckBox)view.findViewById(R.id.glideAudio);
		glideAudioCheckBox.setChecked(module.audioSpeed);
		glideAudioCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				module.audioSpeed = isChecked;
				instrument.moduleUpdated(module);
			}
		});

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.glideUpCC.cc == cc) {
			if (view != null) {
				SeekBar envAttack = (SeekBar) view.findViewById(R.id.glideUp);
				envAttack.setProgress((int) (Math.sqrt(module.glideUp) * 100.0));
			}
		}
		if (module.glideDownCC.cc == cc) {
			if (view != null) {
				SeekBar envDecay = (SeekBar) view.findViewById(R.id.glideDown);
				envDecay.setProgress((int) (Math.sqrt(module.glideDown) * 100.0));
			}
		}
	}
	
}
