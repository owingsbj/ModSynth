package com.gallantrealm.modsynth.viewer;

import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MidiControlDialog;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Envelope;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.ClientModel;
import android.graphics.Canvas;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;

public class EnvelopeViewer extends ModuleViewer {

	Envelope module;

	public EnvelopeViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Envelope) module;
	}

	public static final double HIGHEST = 1.0;
	public static final double LOWEST = 0.0;

	public void drawDiagram(Canvas canvas, float x, float y) {
		path.reset();
		path.moveTo(x - 30, y + 25);
		path.lineTo(x - 20, y - 25);
		path.lineTo(x - 15, y - 5);
		path.lineTo(x + 15, y - 5);
		path.lineTo(x + 30, y + 25);
		canvas.drawPath(path, diagramPaint);
	}

	@Override
	public int getViewResource() {
		return R.layout.envelopepane;
	}

	@Override
	public void onViewCreate(MainActivity mainActivity) {

		View delayRow = view.findViewById(R.id.envDelayRow);
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			delayRow.setVisibility(View.VISIBLE);
			SeekBar envDelay = (SeekBar) view.findViewById(R.id.envDelay);
			envDelay.setProgress((int) (module.delay * 100));
			envDelay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.delay = progress / 100.0;
					instrument.moduleUpdated(module);
				}
			});
			((View) envDelay.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.delayCC));
		} else {
			delayRow.setVisibility(View.GONE);
		}

		SeekBar envAttack = (SeekBar) view.findViewById(R.id.envAttack);
		envAttack.setProgress((int) (100 * Math.sqrt(module.attack)));
		envAttack.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.attack = progress * progress / 10000.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View) envAttack.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.attackCC));

		View holdRow = view.findViewById(R.id.envHoldRow);
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			holdRow.setVisibility(View.VISIBLE);
			SeekBar envHold = (SeekBar) view.findViewById(R.id.envHold);
			envHold.setProgress((int) (module.hold * 100));
			envHold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					module.hold = progress / 100.0;
					instrument.moduleUpdated(module);
				}
			});
			((View) envHold.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.holdCC));
		} else {
			holdRow.setVisibility(View.GONE);
		}

		SeekBar envDecay = (SeekBar) view.findViewById(R.id.envDecay);
		envDecay.setProgress((int) (100 * Math.sqrt(module.decay)));
		envDecay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.decay = progress * progress / 10000.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View) envDecay.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.decayCC));

		SeekBar envSustain = (SeekBar) view.findViewById(R.id.envSustain);
		envSustain.setProgress((int) (100 * module.sustain));
		envSustain.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.sustain = progress / 100.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View) envSustain.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.sustainCC));

		SeekBar envRelease = (SeekBar) view.findViewById(R.id.envRelease);
		envRelease.setProgress((int) (100 * Math.sqrt(module.release)));
		envRelease.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				module.release = progress * progress / 10000.0f;
				instrument.moduleUpdated(module);
			}
		});
		((View) envRelease.getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.releaseCC));

		View slopeTypeRow = view.findViewById(R.id.envSlopeRow);
		if (ClientModel.getClientModel().isFullVersion() || ClientModel.getClientModel().isGoggleDogPass()) {
			slopeTypeRow.setVisibility(View.VISIBLE);
			final Spinner slopeTypeSpinner = (Spinner) view.findViewById(R.id.envSlopeSpinner);
			ArrayAdapter<CharSequence> octaveAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, Envelope.SlopeType.values());
			octaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			slopeTypeSpinner.setAdapter(octaveAdapter);
			slopeTypeSpinner.setSelection(module.slopeType != null ? module.slopeType.ordinal() : 0);
			slopeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
					if (module.slopeType != (Envelope.SlopeType) slopeTypeSpinner.getSelectedItem()) {
						module.slopeType = (Envelope.SlopeType) slopeTypeSpinner.getSelectedItem();
						instrument.moduleUpdated(module);
					}
				}
				public void onNothingSelected(AdapterView av) {
				}
			});
			((View) slopeTypeSpinner.getParent().getParent()).setOnLongClickListener(MidiControlDialog.newLongClickListener(module.slopeTypeCC));

			CheckBox velocitySensitiveCheckBox = (CheckBox) view.findViewById(R.id.envVelocitySensitive);
			velocitySensitiveCheckBox.setChecked(module.velocitySensitive);
			velocitySensitiveCheckBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton arg0, boolean checked) {
					module.velocitySensitive = checked;
					instrument.moduleUpdated(module);
				}
			});
			velocitySensitiveCheckBox.setOnLongClickListener(MidiControlDialog.newLongClickListener(module.velocitySensitiveCC));
		} else {
			slopeTypeRow.setVisibility(View.GONE);
		}

	}

	@Override
	public void updateCC(int cc, double value) {
		if (module.delayCC.cc == cc) {
			SeekBar envDelay = (SeekBar) view.findViewById(R.id.envDelay);
			envDelay.setProgress((int) (module.delay * 100));
		}
		if (module.attackCC.cc == cc) {
			SeekBar envAttack = (SeekBar) view.findViewById(R.id.envAttack);
			envAttack.setProgress((int) (Math.sqrt(module.attack) * 100.0));
		}
		if (module.holdCC.cc == cc) {
			SeekBar envHold = (SeekBar) view.findViewById(R.id.envHold);
			envHold.setProgress((int) (module.hold * 100));
		}
		if (module.decayCC.cc == cc) {
			SeekBar envDecay = (SeekBar) view.findViewById(R.id.envDecay);
			envDecay.setProgress((int) (Math.sqrt(module.decay) * 100.0));
		}
		if (module.sustainCC.cc == cc) {
			SeekBar envSustain = (SeekBar) view.findViewById(R.id.envSustain);
			envSustain.setProgress((int) (module.sustain * 100.0));
		}
		if (module.releaseCC.cc == cc) {
			SeekBar envRelease = (SeekBar) view.findViewById(R.id.envRelease);
			envRelease.setProgress((int) (Math.sqrt(module.release) * 100.0));
		}
		if (module.slopeTypeCC.cc == cc) {
			view.post(new Runnable() {
				@Override
				public void run() {
					final Spinner slopeTypeSpinner = (Spinner) view.findViewById(R.id.envSlopeSpinner);
					slopeTypeSpinner.setSelection(module.slopeType != null ? module.slopeType.ordinal() : 0);
				}
			});
		}
		if (module.velocitySensitiveCC.cc == cc) {
			CheckBox velocitySensitiveCheckBox = (CheckBox) view.findViewById(R.id.envVelocitySensitive);
			velocitySensitiveCheckBox.setChecked(module.velocitySensitive);
		}
	}

}
