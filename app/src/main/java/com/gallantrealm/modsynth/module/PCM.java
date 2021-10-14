package com.gallantrealm.modsynth.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.nio.BufferUnderflowException;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import com.gallantrealm.modsynth.AssetLoader;
import com.gallantrealm.modsynth.Instrument;
import jp.kshoji.com.sun.media.sound.ModelByteBuffer;
import jp.kshoji.com.sun.media.sound.SF2GlobalRegion;
import jp.kshoji.com.sun.media.sound.SF2Layer;
import jp.kshoji.com.sun.media.sound.SF2LayerRegion;
import jp.kshoji.com.sun.media.sound.SF2Region;
import jp.kshoji.com.sun.media.sound.SF2Sample;
import jp.kshoji.com.sun.media.sound.SF2Soundbank;
import uk.co.labbookpages.WavFile;

public class PCM extends Module {
	private static final long serialVersionUID = 1L;

	static final double K64 = 65536;

	public boolean loop;
	public double duty = 0.5;
	public int octave = 0;
	public int pitch = 0;
	public double detune = 0.0;
	public String sampleName;
	public int loopStart;
	public int loopEnd;
	public int patchNum; // for sf2 files with multiple instruments
	public boolean new1 = true;
	public CC octaveCC;
	public CC pitchCC;
	public CC detuneCC;

	public PCM() {
	}

	@Override
	public int getInputCount() {
		return 1;
	}

	@Override
	public int getModCount() {
		return 1;
	}

	@Override
	public int getOutputCount() {
		return 2;
	}

	@Override
	public String getInputName(int n) {
		return "Pitch";
	}

	@Override
	public String getModName(int n) {
		return "Gate";
	}

	@Override
	public String getOutputName(int n) {
		if (n == 0) {
			return "Out L";
		} else {
			return "Out R";
		}
	}

	public static class Sample {
		public int noteOffset;
		private short[] sarray;
		private ShortBuffer sbuffer;
		private ModelByteBuffer modelByteBuffer;
		private SoftReference<short[]> softarray;
		public int sampleRate;
		public int mode;
		public long startLoopOffset;
		public long endLoopOffset;
		public double tune;
		public float attack;
		public float decay;
		public float sustain;
		public float release;
		public Sample(int noteOffset, short[] sarray, int sampleRate, int mode, long startLoopOffset, long endLoopOffset, double tune) {
			this.noteOffset = noteOffset;
			this.sarray = sarray;
			this.sampleRate = sampleRate;
			this.mode = mode;
			this.startLoopOffset = startLoopOffset << 16;
			this.endLoopOffset = endLoopOffset << 16;
			this.tune = tune;
			this.sustain = 1.0f;
			this.release = 1000.0f;
		}
		public Sample(int noteOffset, ShortBuffer sbuffer, ModelByteBuffer modelByteBuffer, int sampleRate, int mode, long startLoopOffset, long endLoopOffset, double tune, float attack, float decay, float sustain, float release) {
			this.noteOffset = noteOffset;
			this.sbuffer = sbuffer;
			this.modelByteBuffer = modelByteBuffer;
			this.sampleRate = sampleRate;
			this.mode = mode;
			this.startLoopOffset = startLoopOffset << 16;
			this.endLoopOffset = endLoopOffset << 16;
			this.tune = tune;
			this.attack = attack;
			this.decay = decay;
			this.sustain = sustain;
			this.release = release;
		}
		public short[] getData() {
			if (sarray != null) {
				return sarray;
			} else {
				if (sbuffer.hasArray()) {
					return sbuffer.array();
				} else {
					if (softarray != null) {
						short[] array = softarray.get();
						if (array != null) {
							return array;
						}
					}
					sbuffer.rewind();
					short[] array = new short[sbuffer.capacity()];
					if (array.length > 0) {
						try {
							sbuffer.get(array);
						} catch (BufferUnderflowException e) {
							// happens sometimes
						}
					}
					softarray = new SoftReference<short[]>(array);
					return array;
				}
			}
		}
	}

	public static class Note {
		public Sample sampleL;
		public Sample sampleR;
	}

	public transient String[] patchNames;
	public transient Note[] notes;
	public transient Sample[] voiceSampleL;
	public transient Sample[] voiceSampleR;
	public transient float[] pcmTune;
	public transient short[][] voiceArrayL;
	public transient short[][] voiceArrayR;

	transient long[] fp_freqL;
	transient long[] fp_phaseL;
	transient long[] fp_freqR;
	transient long[] fp_phaseR;
	transient float[] volume;
	transient double[] lastGate;
	transient boolean[] playing;
	transient boolean[] attacking;
	transient boolean[] sustaining;
	transient float[] lastPitch;

	@Override
	public boolean requiresUpgrade() {
		return !new1;
	}

	@Override
	public void initialize(int voices, int sampleRate) {
		super.initialize(voices, sampleRate);
		voiceSampleL = new Sample[voices];
		voiceSampleR = new Sample[voices];
		voiceArrayL = new short[voices][];
		voiceArrayR = new short[voices][];
		fp_freqL = new long[voices];
		fp_phaseL = new long[voices];
		fp_freqR = new long[voices];
		fp_phaseR = new long[voices];
		volume = new float[voices];
		lastGate = new double[voices];
		playing = new boolean[voices];
		attacking = new boolean[voices];
		sustaining = new boolean[voices];
		pcmTune = new float[voices];
		lastPitch = new float[voices];
		for (int v = 0; v < voices; v++) {
			lastPitch[v] = -1000;
		}
		new1 = true;
		if (octaveCC == null) {
			octaveCC = new CC();
		}
		if (pitchCC == null) {
			pitchCC = new CC();
		}
		if (detuneCC == null) {
			detuneCC = new CC();
		}
		try {
			loadSample();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void terminate() {
		unloadBuffers();
	}

	private void unloadBuffers() {
		if (notes != null) {
			try {
				for (Note note : notes) {
					if (note.sampleL != null && note.sampleL.modelByteBuffer != null) {
						note.sampleL.modelByteBuffer.unload();
						note.sampleL.modelByteBuffer = null;
					}
					if (note.sampleR != null && note.sampleR.modelByteBuffer != null) {
						note.sampleR.modelByteBuffer.unload();
						note.sampleR.modelByteBuffer = null;
					}
				}
			} catch (Exception e) {
			}
		}
	}

	private File createFileFromInputStream(InputStream inputStream) {
		try {
			File f = File.createTempFile("temp", ".sf2");
			f.deleteOnExit();
			OutputStream outputStream = new FileOutputStream(f);
			byte buffer[] = new byte[1024];
			int length = 0;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			outputStream.close();
			inputStream.close();
			return f;
		} catch (IOException e) {
			// Logging exception
		}
		return null;
	}

	public void loadSample() throws Exception {
		unloadBuffers();
		notes = new Note[128];
		patchNames = new String[0];
		for (int i = 0; i < lastPitch.length; i++) { // need to reset so doEnvelope will pick up new samples
			lastPitch[i] = -1000;
		}
		if (sampleName != null) {
			if (sampleName.toLowerCase().endsWith(".sf2")) { // SOUNDFONT

				// NOTE: In some SF2 editors, layers are referred to as Instruments, and instruments are referred to as Patches.

				System.out.println("Soundfont file is " + sampleName);

				File file = new File(sampleName);
				InputStream is;
				if (!file.exists()) {
					// if file not found, perhaps it has a built-in replacement, so try assets
					sampleName = trimName(sampleName);
					System.out.println("Looking for sf2 in assets as " + sampleName);
					is = AssetLoader.loadAsset(sampleName);
					file = createFileFromInputStream(is);
				}
				if (file == null) {
					return; // no sample file specified
				}
				System.out.println("Loading soundfont!");
				SF2Soundbank sf = new SF2Soundbank(file);
				System.out.println("There are " + sf.getInstruments().length + " instruments, " + sf.getLayers().length + " layers, " + sf.getSamples().length + " samples, " + sf.getResources().length + " resources");
				patchNames = new String[sf.getLayers().length];
				for (int i = 0; i < sf.getLayers().length; i++) {
					patchNames[i] = sf.getLayers()[i].getName();
				}
				if (patchNum > sf.getLayers().length) {
					patchNum = 0;
				}
				SF2Layer layer = sf.getLayers()[patchNum];
				System.out.println("Layer " + layer.getName() + " is selected.");
				SF2GlobalRegion globalRegion = layer.getGlobalRegion();
				if (globalRegion != null) {
					System.out.println("Layer has global region: " + globalRegion.getGenerators());
				}
				List<SF2LayerRegion> regions = layer.getRegions();
				for (SF2LayerRegion region : regions) {
					SF2Sample sample = region.getSample();
					int originalKey = sample.getOriginalPitch() > 127 ? 60 : sample.getOriginalPitch();
					int startLoopOffset = (int) sample.getStartLoop();
					int endLoopOffset = (int) sample.getEndLoop();
					int startKey = 0;
					int endKey = 127;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_OVERRIDINGROOTKEY)) {
						originalKey = region.getGenerators().get(SF2LayerRegion.GENERATOR_OVERRIDINGROOTKEY);
						// region.getGenerators().remove(SF2LayerRegion.GENERATOR_OVERRIDINGROOTKEY);
					}
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_KEYRANGE)) {
						startKey = (region.getGenerators().get(SF2LayerRegion.GENERATOR_KEYRANGE) & 0xFF00) >> 8;
						endKey = (region.getGenerators().get(SF2LayerRegion.GENERATOR_KEYRANGE) & 0xFF);
						if (startKey > endKey) {
							int t = endKey;
							endKey = startKey;
							startKey = t;
						}
						// region.getGenerators().remove(SF2LayerRegion.GENERATOR_KEYRANGE);
					}
					int pan = 0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_PAN)) {
						pan = region.getGenerators().get(SF2LayerRegion.GENERATOR_PAN);
						// region.getGenerators().remove(SF2LayerRegion.GENERATOR_PAN);
					}
					double tune = 0.0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_COARSETUNE)) {
						tune += region.getGenerators().get(SF2LayerRegion.GENERATOR_COARSETUNE) / 12.0;
						// region.getGenerators().remove(SF2LayerRegion.GENERATOR_COARSETUNE);
					}
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_FINETUNE)) {
						tune += region.getGenerators().get(SF2LayerRegion.GENERATOR_FINETUNE) / 12.0 / 100.0;
						// region.getGenerators().remove(SF2LayerRegion.GENERATOR_FINETUNE);
					}
					int mode = 0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_SAMPLEMODES)) {
						mode = region.getGenerators().get(SF2LayerRegion.GENERATOR_SAMPLEMODES) & 0x01; // only last 2 bits used, last bit indicates loop
//						region.getGenerators().remove(SF2LayerRegion.GENERATOR_SAMPLEMODES);
					} else {
						if (globalRegion != null) {
							if (globalRegion.getGenerators().containsKey(SF2Region.GENERATOR_SAMPLEMODES)) {
								mode = globalRegion.getGenerators().get(SF2Region.GENERATOR_SAMPLEMODES) & 0x01;
							}
						}
					}
					float attack = 0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_ATTACKVOLENV)) {
						attack = region.getGenerators().get(SF2LayerRegion.GENERATOR_ATTACKVOLENV);
					} else if (globalRegion != null && globalRegion.getGenerators().containsKey(SF2Region.GENERATOR_ATTACKVOLENV)) {
						attack = globalRegion.getGenerators().get(SF2Region.GENERATOR_ATTACKVOLENV);
					}
					attack = (float) Math.pow(2, attack / 1200);
					float decay = 0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_DECAYVOLENV)) {
						decay = region.getGenerators().get(SF2LayerRegion.GENERATOR_DECAYVOLENV);
					} else if (globalRegion != null && globalRegion.getGenerators().containsKey(SF2Region.GENERATOR_DECAYVOLENV)) {
						decay = globalRegion.getGenerators().get(SF2Region.GENERATOR_DECAYVOLENV);
					}
					decay = (float) Math.pow(2, decay / 1200);
					float sustain = 0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_SUSTAINVOLENV)) {
						sustain = 1 - region.getGenerators().get(SF2LayerRegion.GENERATOR_SUSTAINVOLENV) / 1000.0f;
					} else if (globalRegion != null && globalRegion.getGenerators().containsKey(SF2Region.GENERATOR_SUSTAINVOLENV)) {
						sustain = 1 - globalRegion.getGenerators().get(SF2Region.GENERATOR_SUSTAINVOLENV) / 1000.0f;
					}
					float release = 0;
					if (region.getGenerators().containsKey(SF2LayerRegion.GENERATOR_RELEASEVOLENV)) {
						release = region.getGenerators().get(SF2LayerRegion.GENERATOR_RELEASEVOLENV);
					} else if (globalRegion != null && globalRegion.getGenerators().containsKey(SF2Region.GENERATOR_RELEASEVOLENV)) {
						release = globalRegion.getGenerators().get(SF2Region.GENERATOR_RELEASEVOLENV);
					}
					release = (float) Math.pow(2, release / 1200);
					if (startKey >= 0) {
						if (sample.getFormat().getEncoding() == jp.kshoji.javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED) {
							try {
								ModelByteBuffer modelByteBuffer = sample.getDataBuffer();
								ArrayList<ModelByteBuffer> modelByteBuffers = new ArrayList<ModelByteBuffer>();
								modelByteBuffers.add(modelByteBuffer);
								ModelByteBuffer.loadAll(modelByteBuffers);
								ShortBuffer buffer = modelByteBuffer.asShortBuffer();
								int rate = (int) sample.getFormat().getSampleRate();
								String name = sample.getName();
								System.out.println("    Sample " + name + " originalKey " + originalKey + " rate " + rate + " length " + buffer.capacity() + " mode " + mode + " loop " + startLoopOffset + "-" + endLoopOffset + " pan " + pan
										+ " keys " + startKey + "-" + endKey + " detune " + tune + " attack " + attack + " decay " + decay + " sustain " + sustain + " release " + release);
								if (sample.getPitchCorrection() != 0) {
									System.out.println("      has pitch correction of " + sample.getPitchCorrection());
								}
								for (int key = startKey; key <= endKey; key++) {
									int offset = key - originalKey;
									if (notes[key] == null) {
										notes[key] = new Note();
									}
									Sample s = new Sample(offset, buffer, modelByteBuffer, rate, mode, startLoopOffset, endLoopOffset, tune, attack, decay, sustain, release);
									if (pan < 0) {
										notes[key].sampleL = s;
									} else if (pan > 0) {
										notes[key].sampleR = s;
									} else {
										notes[key].sampleL = s;
										notes[key].sampleR = s;
									}

									// TODO handle different velocity samples

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							throw new Exception("Encoding not supported: " + sample.getFormat().getEncoding());
						}
					}
				}
				System.out.println("Loaded PCM samples successfully.");

			} else { // WAV

				System.out.println("Loading wav file: " + sampleName);
				File file;
				if (sampleName.startsWith("file:")) { // via an external url
					file = new File(sampleName.substring(7));
				} else if (sampleName.startsWith("/")) { // full path file
					file = new File(sampleName);
				} else { // within the application
					file = null;
				}
				InputStream is;
				long len;
				if (file != null && file.exists()) {
					is = new FileInputStream(file);
					len = file.length();
				} else {
					// if file not found, perhaps it has a built-in replacement, so try assets
					System.out.println("Looking for wav in assets");
					file = null;
					sampleName = trimName(sampleName);
					is = AssetLoader.loadAsset(sampleName);
					len = AssetLoader.getAssetLength(sampleName);
				}
				WavFile wavFile = WavFile.openWavFile(is, len);
				System.out.println("WAV file has " + wavFile.getNumChannels() + " channels of length " + wavFile.getNumFrames() + " sample rate " + wavFile.getSampleRate());
				int waveLength = Math.min((int) wavFile.getNumFrames(), 1000000);
				int channels = wavFile.getNumChannels();
				double[][] wavedata = new double[channels][waveLength];
				wavFile.readFrames(wavedata, waveLength);
				short[] buffer = new short[waveLength];
				for (int i = 0; i < waveLength; i++) {
					buffer[i] = (short) (wavedata[0][i] * 32767);
				}
				short[] buffer2;
				if (channels == 1) {
					buffer2 = buffer; // mono
				} else { // stereo or beyond
					buffer2 = new short[waveLength];
					for (int i = 0; i < waveLength; i++) {
						buffer2[i] = (short) (wavedata[1][i] * 32767);
					}
				}
				for (int i = 0; i < notes.length; i++) {
					notes[i] = new Note();
					notes[i].sampleL = new Sample(i - 60, buffer, (int) wavFile.getSampleRate(), loop ? 1 : 0, 0, buffer.length - 1, 0.0);
					notes[i].sampleR = new Sample(i - 60, buffer2, (int) wavFile.getSampleRate(), loop ? 1 : 0, 0, buffer2.length - 1, 0.0);
				}
				System.out.println("Loaded wav file successfully.");

			}
		}
	}

	@Override
	public void doEnvelope(int voice) {
		float newPitch = (float)(octave * 12 + pitch + detune / 100.0f) / NOTES_PER_VOLT + ((input1 != null) ? input1.value[voice] : 0);
		if (newPitch != lastPitch[voice]) {
			lastPitch[voice] = newPitch;
			int key = (int) (newPitch * 100.0 + 0.5);
			key = (int) Math.max(0, Math.min(notes.length - 1, key));
			float pitchOffsetFromKey = newPitch * 100.0f - key;
			if (notes[key] != null) {
				voiceSampleL[voice] = notes[key].sampleL;
				voiceSampleR[voice] = notes[key].sampleR;
				if (voiceSampleL[voice] != null) {
					voiceArrayL[voice] = voiceSampleL[voice].getData();
					float frequency = (float) Math.pow(2, (pitchOffsetFromKey + voiceSampleL[voice].noteOffset) / 12.0 + voiceSampleL[voice].tune) * pitchToFrequency(36.0f / 100.0f);
					fp_freqL[voice] = (long) (frequency * K64 / 64) * voiceSampleL[voice].sampleRate / sampleRate;
				}
				if (voiceSampleR[voice] != null) {
					voiceArrayR[voice] = voiceSampleR[voice].getData();
					float frequency = (float) Math.pow(2, (pitchOffsetFromKey + voiceSampleR[voice].noteOffset) / 12.0 + voiceSampleR[voice].tune) * pitchToFrequency(36.0f / 100.0f);
					fp_freqR[voice] = (long) (frequency * K64 / 64) * voiceSampleR[voice].sampleRate / sampleRate;
				}
			}
		}
		if (voiceSampleL[voice] != null) {  // TODO consider envelope for each channel
			if (mod1 != null) {
				if (mod1.value[voice] > 0 && lastGate[voice] <= 0) {
					fp_phaseL[voice] = 0;
					fp_phaseR[voice] = 0;
					playing[voice] = true;
					attacking[voice] = true;
					sustaining[voice] = true;
					volume[voice] = 0.0f;
				} else if (mod1.value[voice] <= 0 && lastGate[voice] > 0) {
					attacking[voice] = false;
					sustaining[voice] = false;
				} else {
					if (sustaining[voice]) {
						if (attacking[voice]) {
							if (volume[voice] < 1) {
								if (voiceSampleL[voice].attack <= 0) {
									volume[voice] = 1.0f;
								} else {
									volume[voice] += 1.0 / voiceSampleL[voice].attack / Instrument.ENVELOPE_RATE;
								}
							} else {
								volume[voice] = 1;
								attacking[voice] = false;
							}
						} else {
							if (volume[voice] > voiceSampleL[voice].sustain) {
								volume[voice] -= 1.0 / voiceSampleL[voice].decay / Instrument.ENVELOPE_RATE;
							} else {
								volume[voice] = voiceSampleL[voice].sustain;
							}
						}
					} else {
						if (volume[voice] > 0) {
							if (voiceSampleL[voice].release <= 0) {
								volume[voice] = 0;
							} else {
								volume[voice] -= 1.0 / voiceSampleL[voice].release / Instrument.ENVELOPE_RATE;
							}
						} else {
							volume[voice] = 0;
						}
					}
				}
				lastGate[voice] = mod1.value[voice];
			} else {
				playing[voice] = true;
				volume[voice] = 1.0f;
			}
		}
	}

	@Override
	public void doSynthesis(int startVoice, int endVoice) {
		for (int voice = startVoice; voice <= endVoice; voice++) {
			long newPhaseL = fp_phaseL[voice] + fp_freqL[voice];
			long newPhaseR = fp_phaseR[voice] + fp_freqR[voice];
			fp_phaseL[voice] = newPhaseL;
			fp_phaseR[voice] = newPhaseR;
			Sample sampleL = voiceSampleL[voice];
			Sample sampleR = voiceSampleR[voice];
			if (playing[voice]) {
				if (sampleL != null) {
					short[] arrayL = voiceArrayL[voice];
					if (sampleL.mode == 1) { // loop
						while (fp_phaseL[voice] >= sampleL.endLoopOffset) {
							fp_phaseL[voice] = fp_phaseL[voice] - (sampleL.endLoopOffset - sampleL.startLoopOffset);
						}
					}
					int indexL = (int) (fp_phaseL[voice] >> 16) & 0x7FFFFFFF;
					if (indexL < arrayL.length - 1) {
						// choose two points on sample and obtain weighted average to anti-alias
						float v1 = arrayL[indexL] / 32767.0f * volume[voice];
						float v2 = arrayL[indexL + 1] / 32767.0f * volume[voice];
						float weight = (fp_phaseL[voice] & 0xFFFF) / 65536.0f;
						output1.value[voice] = (1.0f - weight) * v1 + weight * v2;
					} else {
						playing[voice] = false;
						output1.value[voice] = 0.0f;
					}
				}
				if (sampleR != null) {
					short[] arrayR = voiceArrayR[voice];
					if (sampleR.mode == 1) { // loop
						while (fp_phaseR[voice] >= sampleR.endLoopOffset) {
							fp_phaseR[voice] = fp_phaseR[voice] - (sampleR.endLoopOffset - sampleR.startLoopOffset);
						}
					}
					int indexR = (int) (fp_phaseR[voice] >> 16) & 0x7FFFFFFF;
					if (indexR < arrayR.length - 1) {
						// choose two points on sample and obtain weighted average to anti-alias
						float v1 = arrayR[indexR] / 32767.0f * volume[voice];
						float v2 = arrayR[indexR + 1] / 32767.0f * volume[voice];
						float weight = (fp_phaseR[voice] & 0xFFFF) / 65536.0f;
						output2.value[voice] = (1.0f - weight) * v1 + weight * v2;
					} else {
						playing[voice] = false;
						output2.value[voice] = 0.0f;
					}
				}
			} else {
				output1.value[voice] = 0.0f;
				output2.value[voice] = 0.0f;
			}
		}
	}

	private String trimName(String sampleName) {
		if (sampleName.lastIndexOf("/") >= 0) {
			return sampleName.substring(sampleName.lastIndexOf("/") + 1);
		} else {
			return sampleName;
		}
	}

	@Override
	public void updateCC(int cc, double value) {
		if (octaveCC.cc == cc) {
			octave = (int) (octaveCC.range(value) * 10.0) - 5;
		}
		if (pitchCC.cc == cc) {
			pitch = (int) (pitchCC.range(value) * 12.0);
		}
		if (detuneCC.cc == cc) {
			detune = detuneCC.range(value) * 100 - 50;
		}
	}

}
