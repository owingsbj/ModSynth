package com.gallantrealm.modsynth;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class MelodyEditor extends View implements View.OnTouchListener, View.OnLayoutChangeListener {

	float notesHigh;
	float measuresWide;
	int beatsPerMeasure = 16;
	private ArrayList<Note> notes = new ArrayList<Note>();
	private int startBeat;
	private int startNote;
	int currentBeat;
	public int newNoteDuration = 2;
	public float newNoteVelocity = 0.5f;
	public boolean glide = false;

	public boolean dirty;

	public MelodyEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MelodyEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MelodyEditor(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		setOnTouchListener(this);
		addOnLayoutChangeListener(this);
		updateScale(0, 0);
	}

	@Override
	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		updateScale(right - left, bottom - top);
		postInvalidate();
	}

	private void updateScale(int width, int height) {
		if (width > 0 && height > 0) {
			DisplayMetrics metrics = new DisplayMetrics();
			if (this.getContext() instanceof Activity) {
				((Activity) this.getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
				notesHigh = height / metrics.ydpi * 12.0f;
				measuresWide = width / metrics.xdpi * 2.0f;
			} else {
				notesHigh = 24;
				measuresWide = 3;
			}
		} else {
			notesHigh = 25;
			measuresWide = 4;
		}
		System.out.println("UPDATESCALE " + notesHigh + " " + measuresWide);
	}

	public void setMelody(ArrayList<Note> notes, int beatsPerMeasure) {
		this.notes = notes;
		this.beatsPerMeasure = beatsPerMeasure;
		postInvalidate();
	}

	public void setStartBeat(int startBeat) {
		this.startBeat = startBeat;
		postInvalidate();
	}

	public int getStartBeat() {
		return this.startBeat;
	}

	public void setStartNote(int startNote) {
		this.startNote = startNote;
		postInvalidate();
	}

	public int getStartNote() {
		return startNote;
	}

	public Note getSelectedNote() {
		return selectedNote;
	}

	public void deleteNote(Note note) {
		notes.remove(note);
		invalidate();
	}

	public void setCurrentBeat(int currentBeat) {
		this.currentBeat = currentBeat;
		if (currentBeat < startBeat) {
			startBeat = currentBeat / beatsPerMeasure * beatsPerMeasure;
		} else if (currentBeat > startBeat + beatsPerMeasure * measuresWide) {
			startBeat = currentBeat / beatsPerMeasure * beatsPerMeasure;
		}
		postInvalidate();
	}

	public int getCurrentBeat() {
		return currentBeat;
	}

	private final Paint paint = new Paint();

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		try {

			// Note: This drawing is for phone. For tablet, show more octaves and measures
			int w = this.getWidth();
			int h = this.getHeight();

			int noteh = (int) (h / notesHigh);
			int notew = (int) (w / measuresWide / beatsPerMeasure);

			paint.setTextSize(noteh);

			// Draw grey bars for the sharps.
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(0x80C0C0C0);
			for (int i = 0; i <= notesHigh + 1; i++) {
				int note = (48 + i + startNote) % 12;
				if (note == 1 || note == 3 || note == 6 || note == 8 || note == 10) {
					canvas.drawRect(0, h - noteh * i - noteh, w, h - noteh * i, paint);
				}
			}

			// Draw lines to designate the measures
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2);
			paint.setColor(0xFF808080);
			for (int i = 0; i <= (measuresWide + 2) * beatsPerMeasure; i++) {
				if ((i + startBeat) % beatsPerMeasure == 0) {
					canvas.drawLine(notew * i, 0, notew * i, h, paint);
					int measure = (i + startBeat) / beatsPerMeasure;
					canvas.drawText("M" + measure, notew * i + 10, h - 5, paint);
				}
			}

			// Draw lines to designate the octaves
			for (int i = 0; i <= notesHigh + 1; i++) {
				int note = (48 + i + startNote) % 12;
				if (note == 0) {
					canvas.drawLine(0, h - noteh * i, w, h - noteh * i, paint);
					int octave = (48 + i + startNote) / 12;
					canvas.drawText("C" + octave, 10, h - noteh * i - 5, paint);
				}
			}

			if (notes != null) {

				// Draw notes
				Note lastDrawnNote = null;
				paint.setStyle(Paint.Style.STROKE);
				for (int i = -12; i <= (measuresWide + 2) * beatsPerMeasure; i++) {
					for (int j = 0; j < notes.size(); j++) {
						Note note = notes.get(j);
						if (note.start == i + startBeat) {
							if (note == selectedNote) {
								paint.setColor(0xFF4040FF);
							} else {
								paint.setColor(0xFF404040);
							}
							paint.setStrokeWidth(noteh / 2.0f * note.velocity);
							if (note.velocity == 0) {
								paint.setPathEffect(new DashPathEffect(new float[] { 2.0f, 2.0f }, 0));
							} else {
								paint.setPathEffect(null);
							}
							canvas.drawRect(notew * i, h - noteh * (note.pitch - startNote) - noteh, notew * (i + note.duration), h - noteh * (note.pitch - startNote), paint);
							if (note.continuous && lastDrawnNote != null) {
								canvas.drawLine(notew * (i - 1), h - noteh * (lastDrawnNote.pitch - startNote) - noteh, notew * i, h - noteh * (note.pitch - startNote) - noteh, paint);
							}
							lastDrawnNote = note;
						}
					}
				}

				// Shadow draw other instrument's notes in the same section. This helps to harmonize
//				paint.setStyle(Paint.Style.STROKE);
//				paint.setColor(0xFF808000);
//				paint.setStrokeWidth(noteh / 8.0f);
//				for (int m = 0; m < section.melodies.size(); m++) {
//					Instrument instrument = score.instruments.get(m);
//					if (instrument != null && section.melodies.containsKey(instrument)) {
//						Melody otherMelody = section.melodies.get(instrument);
//						if (otherMelody != null && otherMelody != melody) {
//							for (int i = -12; i < measuresWide * beatsPerMeasure; i++) {
//								for (int j = 0; j < otherMelody.notes.size(); j++) {
//									Note note = otherMelody.notes.get(j);
//									if (note.start == i + startBeat) {
//										canvas.drawRect(notew * i, h - noteh * (note.pitch - startNote) - noteh, notew * (i + note.duration), h - noteh * (note.pitch - startNote), paint);
//									}
//								}
//							}
//						}
//					}
//				}

			}

			// draw current beat bar
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(noteh / 8);
			paint.setColor(0xFFFF0000);
			canvas.drawLine(notew * (currentBeat - startBeat - 1), 0, notew * (currentBeat - startBeat - 1), h, paint);

		} catch (Exception e) {
		}
	}

	boolean moved;
	float initialX, initialY;
	int initialStartBeat, initialStartNote;
	int touchPitch;
	int touchBeat, selectedNoteTouchOffset;
	Note selectedNote;

	public boolean onTouch(View view, MotionEvent event) {
		if (notes == null) {
			return false;
		}
		int w = view.getWidth();
		int h = view.getHeight();
		int noteh = (int) (h / notesHigh);
		int notew = (int) (w / measuresWide / beatsPerMeasure);

		// Determine the beat and pitch
		int ai = event.getActionIndex();
		float x = event.getX(ai);
		float y = event.getY(ai);
		int beat = (int) (x / notew + startBeat);
		int pitch = (int) ((h - y) / noteh) + startNote;

		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			moved = false;
			initialX = x;
			initialY = y;
			initialStartBeat = startBeat;
			initialStartNote = startNote;
			touchBeat = beat;
			touchPitch = pitch;
			// Figure out if a note already exists there
			selectedNote = null;
			for (int i = 0; i < notes.size(); i++) {
				Note note = notes.get(i);
				if (beat >= note.start && beat < note.start + note.duration && note.pitch == pitch) {
					selectedNote = note;
					selectedNoteTouchOffset = beat - note.start;
				}
			}
			postInvalidate();
		} else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			if (selectedNote != null) {
				if (selectedNote.start != beat || selectedNote.pitch != pitch) {
					selectedNote.start = Math.max(beat - selectedNoteTouchOffset, 0);
					selectedNote.pitch = Math.min(36, Math.max(-12, pitch));
					postInvalidate();
					dirty = true;
				}
			} else {
				int deltaBeats = ((int) (x - initialX) / notew);
				int deltaNotes = ((int) (y - initialY) / noteh);
				if (deltaBeats != 0 || deltaNotes != 0) {
					moved = true;
				}
				if (moved) {
					startNote = (int) Math.min(37 - notesHigh, Math.max(-12.0f, initialStartNote + deltaNotes));
					startBeat = Math.max(0, initialStartBeat - deltaBeats);
					postInvalidate();
				}
			}
		} else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			if (!moved && selectedNote == null) {
				Note note = new Note();
				note.start = beat - newNoteDuration / 2;
				note.pitch = pitch;
				note.duration = newNoteDuration;
				note.velocity = newNoteVelocity;
				note.continuous = this.glide;
				notes.add(note);
				selectedNote = note;
				postInvalidate();
				dirty = true;
			}
		}

		return true;
	}

}
