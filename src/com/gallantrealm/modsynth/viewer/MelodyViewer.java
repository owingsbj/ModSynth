package com.gallantrealm.modsynth.viewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import com.gallantrealm.android.FileUtils;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.Instrument;
import com.gallantrealm.modsynth.MainActivity;
import com.gallantrealm.modsynth.MelodyEditor;
import com.gallantrealm.modsynth.Note;
import com.gallantrealm.modsynth.R;
import com.gallantrealm.modsynth.module.Melody;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.ClientModel;
import com.gallantrealm.mysynth.MessageDialog;
import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

public class MelodyViewer extends ModuleViewer {

	Melody module;
	boolean maximized;

	public MelodyViewer(Module module, Instrument instrument) {
		super(module, instrument);
		this.module = (Melody) module;
	}

	public void drawDiagram(Canvas canvas, float x, float y) {
		canvas.drawRect(x - 30, y - 25, x + 30, y + 25, paint);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 3; j++) {
				canvas.drawPoint(x - 20 + 10 * i, y - 10 + 10 * j, paint);
			}
		}
	}

	@Override
	public int getViewResource() {
		return R.layout.melodypane;
	}

	transient MelodyEditor melodyEditor;

	@Override
	public void onViewCreate(final MainActivity mainActivity) {

		melodyEditor = (MelodyEditor) view.findViewById(R.id.melodyEditor);
		melodyEditor.setMelody(module.notes, module.beatsPerMeasure);

		Button shorterButton = (Button) view.findViewById(R.id.shorterButton);
		shorterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Note note = melodyEditor.getSelectedNote();
				if (note != null && note.duration > 1) {
					note.duration--;
					melodyEditor.invalidate();
				}
			}
		});
		Button longerButton = (Button) view.findViewById(R.id.longerButton);
		longerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Note note = melodyEditor.getSelectedNote();
				if (note != null && note.duration < module.beatsPerMeasure) {
					note.duration++;
					melodyEditor.invalidate();
				}
			}
		});
		Button softerButton = (Button) view.findViewById(R.id.softerButton);
		softerButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Note note = melodyEditor.getSelectedNote();
				if (note != null && note.velocity > 0) {
					note.velocity -= 0.125;
					melodyEditor.invalidate();
				}
			}
		});
		Button louderButton = (Button) view.findViewById(R.id.louderButton);
		louderButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Note note = melodyEditor.getSelectedNote();
				if (note != null && note.velocity < 1) {
					note.velocity += 0.125;
					melodyEditor.invalidate();
				}
			}
		});
		Button glideButton = (Button) view.findViewById(R.id.glideButton);
		glideButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Note note = melodyEditor.getSelectedNote();
				if (note != null) {
					note.continuous = !note.continuous;
					melodyEditor.invalidate();
				}
			}
		});
		Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Note note = melodyEditor.getSelectedNote();
				if (note != null) {
					melodyEditor.deleteNote(note);
					melodyEditor.invalidate();
				}
			}
		});

		final Button maximizeButton = (Button) view.findViewById(R.id.maximize);
		maximizeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (maximized) {
					mainActivity.modGraphPane.setVisibility(View.VISIBLE);
					mainActivity.operatorPane.setVisibility(View.VISIBLE);
					maximizeButton.setText("[+]");
					maximized = false;
				} else {
					mainActivity.modGraphPane.setVisibility(View.GONE);
					mainActivity.operatorPane.setVisibility(View.GONE);
					maximizeButton.setText("[-]");
					maximized = true;
				}
			}
		});

		Button showControlsButton = (Button) view.findViewById(R.id.showControls);
		showControlsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				view.findViewById(R.id.pianoRoll).setVisibility(View.GONE);
				view.findViewById(R.id.melodyControls).setVisibility(View.VISIBLE);
			}
		});
		Button showEditorButton = (Button) view.findViewById(R.id.showEditor);
		showEditorButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				view.findViewById(R.id.pianoRoll).setVisibility(View.VISIBLE);
				view.findViewById(R.id.melodyControls).setVisibility(View.GONE);
			}
		});
		view.findViewById(R.id.pianoRoll).setVisibility(View.VISIBLE);
		view.findViewById(R.id.melodyControls).setVisibility(View.GONE);

		final Spinner voicesSpinner = (Spinner) view.findViewById(R.id.melodyVoices);
		ArrayAdapter<CharSequence> voicesAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" });
		voicesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		voicesSpinner.setAdapter(voicesAdapter);
		voicesSpinner.setSelection(Math.max(0, module.voices - 1));
		voicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView av, View v, int arg2, long arg3) {
				if (module.voices != voicesSpinner.getSelectedItemPosition() + 1) {
					module.voices = voicesSpinner.getSelectedItemPosition() + 1;
					instrument.moduleUpdated(module);
				}
			}
			public void onNothingSelected(AdapterView av) {
			}
		});

		final CheckBox loopCheckbox = (CheckBox) view.findViewById(R.id.melodyLoopCheckBox);
		loopCheckbox.setChecked(module.looping);
		loopCheckbox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				module.looping = loopCheckbox.isChecked();
			}
		});
		final CheckBox retriggerCheckbox = (CheckBox) view.findViewById(R.id.melodyRetriggerCheckBox);
		retriggerCheckbox.setChecked(module.retrigger);
		retriggerCheckbox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				module.retrigger = retriggerCheckbox.isChecked();
			}
		});
		final Button midiLoadButton = (Button) view.findViewById(R.id.melodyLoad);
		if (!ClientModel.getClientModel().isGoggleDogPass()) {
			midiLoadButton.setVisibility(View.GONE);
		}
		midiLoadButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= 23 && mainActivity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
					mainActivity.requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, MainActivity.REQUEST_PERMISSION_READ_PCM_EXTERNAL_STORAGE);
					return;
				}
				onContinueMidiFileSelect(mainActivity);
			}
		});
	}

	public void onContinueMidiFileSelect(MainActivity mainActivity) {
		Intent intent = new Intent();
		intent.setType("*/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		mainActivity.startActivityForResult(Intent.createChooser(intent, Translator.getTranslator().translate("Select a MIDI file using")), 0);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		String path = "";
		try {
			Uri mImageCaptureUri = data.getData();
			System.out.println("URI: " + mImageCaptureUri);
			path = FileUtils.getPath(view.getContext(), mImageCaptureUri);
			loadMidiFileSample(path);
		} catch (Exception e) {
			e.printStackTrace();
			MessageDialog messageDialog = new MessageDialog(view.getRootView().getContext(), null, "The file cannot be opened at " + path, new String[] { "OK" });
			messageDialog.show();
		}
	}

	public void loadMidiFileSample(String path) throws IOException {
		File input = new File(path);
		MidiFile midi = new MidiFile(input);
		if (midi.getTrackCount() == 0) {
			MessageDialog messageDialog = new MessageDialog(view.getRootView().getContext(), null, "No MIDI data found in file.  Is it a MIDI file?", new String[] { "OK" });
			messageDialog.show();
			return;
		}
		
		// TODO: add ability to select track and subsection
		
		module.notes = new ArrayList<Note>();
		ArrayList<Note> unfinishedNotes = new ArrayList<Note>();
		int resolution = midi.getResolution(); // ticks per quarter note (beat)
		int minStart = 100000;
		MidiTrack track = midi.getTracks().get(Math.min(midi.getTrackCount()-1, 1));
		Iterator<MidiEvent> it = track.getEvents().iterator();
		while (it.hasNext()) {
			MidiEvent event = it.next();
			if (event instanceof NoteOn) {
				NoteOn noteOn = (NoteOn) event;
				Note note = new Note();
				note.start = (int) (event.getTick() * module.beatsPerMeasure / resolution / 4);
				note.pitch = noteOn.getNoteValue() - 48;
				note.velocity = noteOn.getVelocity() / 127.0f;
				note.duration = 1;
				if (note.velocity > 0) { // a true note on
					module.notes.add(note);
					unfinishedNotes.add(note);
				} else {  // an off note, find the corresponding on note
					for (Note unfinishedNote : unfinishedNotes) {
						if (unfinishedNote.pitch == note.pitch) {
							unfinishedNote.duration = note.start - unfinishedNote.start;
							unfinishedNotes.remove(unfinishedNote);
							break;
						}
					}
				}
				minStart = Math.min(note.start, minStart);
			} else if (event instanceof NoteOff) {
				NoteOff noteOff = (NoteOff)event;
				int pitch = noteOff.getNoteValue() - 48;
				int end = (int) (event.getTick() * module.beatsPerMeasure / resolution / 4);
				for (Note unfinishedNote : unfinishedNotes) {
					if (unfinishedNote.pitch == pitch) {
						unfinishedNote.duration = end - unfinishedNote.start;
						unfinishedNotes.remove(unfinishedNote);
						break;
					}
				}
			}
		}
		// adjust to get the start at zero
		for (Note note : module.notes) {
			note.start = note.start - minStart;
		}
//		System.out.println("The new stuff:");
//		for (Note note : module.notes) {
//			System.out.println("  Note: start = " + note.start +"  pitch = "+note.pitch+"  velocity = " + note.velocity +"  duration = "+note.duration);
//		}
		melodyEditor.setMelody(module.notes, module.beatsPerMeasure);
	}

	public void setCurrentBeat(Integer step) {
		if (melodyEditor != null) {
			melodyEditor.setCurrentBeat(step);
		}
	}

}
