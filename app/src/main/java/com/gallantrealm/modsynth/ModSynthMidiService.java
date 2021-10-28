package com.gallantrealm.modsynth;

import android.annotation.SuppressLint;
import android.media.midi.MidiDeviceService;
import android.media.midi.MidiDeviceStatus;
import android.media.midi.MidiReceiver;
import android.view.View;
import android.widget.Toast;

import com.gallantrealm.android.MessageDialog;
import com.gallantrealm.android.Translator;
import com.gallantrealm.modsynth.module.Module;
import com.gallantrealm.mysynth.MySynth;
import com.gallantrealm.mysynth.MySynthMidi;
import com.gallantrealm.mysynth.MySynthMidiService;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

@SuppressLint("NewApi")
public class ModSynthMidiService extends MidiDeviceService {

    ClientModel clientModel;
    MySynthMidiService mySynthMidiService;
    MySynth synth;

    @Override
    public void onCreate() {
        System.out.println(">>ModSynthMidiService.onCreate");
        if (synth != null) {
            System.out.println("  ModSynthMidiService.onCreate already created");
        } else {
            create();
        }
        super.onCreate();
        System.out.println("<<ModSynthMidiService.onCreate");
    }

    private void create() {
        clientModel = ClientModel.getClientModel();
        clientModel.setContext(ModSynthMidiService.this);
        synth = MySynth.create(this.getApplication());
        mySynthMidiService = new MySynthMidiService(this, synth, new MySynthMidi.Callbacks() {
            public void onDeviceAttached(String s) {
                // not called
            }
            public void onDeviceDetached(String s) {
                // not called
            }
            public void onProgramChange(int programNum) {
                System.out.println(">>ModSynthMidiService.onProgramChange "+programNum);
                String soundName = clientModel.findObject("" + programNum + " ", ".modsynth");
                if (soundName == null) {
//                    Toast.makeText(ModSynthMidiService.this.getApplication(),
//                            "No instrument found with name starting with \"" + programNum + " \" ",
//                            Toast.LENGTH_LONG).show();
                } else {
                    loadInstrument(soundName);
                }
                System.out.println("<<ModSynthMidiService.onProgramChange ");
            }
            public void onControlChange(int control, int value) {
                System.out.println("<>ModSynthMidiService.onControlChange "+control+" "+value);
                if (synth.getInstrument() != null) {
                    synth.getInstrument().controlChange(control, value / 127.0f);
                }
            }
            public void onTimingClock() {
                // not implemented yet..
            }
            public void onSysex(byte[] bytes) {
                // nothing defined for this in ModSynth
            }
        });
    }

    @Override
    public void onDestroy() {
        System.out.println(">>ModSynthMidiService.onDestroy");
        super.onDestroy();
        if (synth == null) {
            System.out.println("  ModSynthMidiService.onDestroy already destroyed");
        } else {
            mySynthMidiService.terminate();
            mySynthMidiService = null;
            synth.terminate();
            synth = null;
        }
        System.out.println("<<ModSynthMidiService.onDestroy");
    }

    @Override
    public MidiReceiver[] onGetInputPortReceivers() {
        return new MidiReceiver[] {mySynthMidiService.getMidiReceiver()};
    }

    @Override
    @SuppressLint("NewApi")
    public void onDeviceStatusChanged(MidiDeviceStatus status) {
        if (status.isInputPortOpen(0)) {
            inputPortOpen();
        } else if (!status.isInputPortOpen(0)){
            inputPortClosed();
        }
    }

    @Override
    public void onClose() {
        System.out.println("<>ModSynthMidiService.onClose");
    }

    private void inputPortOpen() {
        System.out.println(">>ModSynthMidiService.inputPortOpen");
        try {
            if (synth == null) {
                create();
            }
            // load default instrument
            clientModel.loadPreferences();
            String soundName = clientModel.getInstrumentName();
            if (soundName == null || soundName.startsWith("com.gallantrealm")) { // it isn't a sound
                soundName = "BuiltIn/Basic";
            }
            loadInstrument(soundName);

            synth.start();
            mySynthMidiService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("<<ModSynthMidiService.inputPortOpen");
    }

    private void inputPortClosed() {
        System.out.println(">>ModSynthMidiService.inputPortClosed");
        if (mySynthMidiService != null) {
            mySynthMidiService.stop();
        }
        if (synth != null) {
            synth.stop();
        }
        System.out.println("<<ModSynthMidiService.inputPortClosed");
    }

    private void loadInstrument(final String soundName) {
        System.out.println(">>ModSynthMidiService.loadInstrument: " + soundName);
//        Thread thread = new Thread() {
//            public void run() {

                Instrument sound = null;
                if (soundName.startsWith("file://")) { // playing a sent sound
                    sound = (Instrument) clientModel.loadObject(soundName, true);
                } else if (soundName.startsWith("BuiltIn/")) {
                    try {
                        String filename = soundName.substring("BuiltIn/".length()).trim();
                        filename = "Instruments/" + filename;
                        InputStream is = ModSynthMidiService.this.getApplication().getAssets().open(filename + ".modsynth");
                        ObjectInputStream inStream = new ObjectInputStream(is);
                        sound = (Instrument) inStream.readObject();
                        inStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    String fileName = soundName + ".modsynth";
                    sound = (Instrument) clientModel.loadObject(fileName, true);
                }
                if (sound == null) {
                    String msg = soundName + " " + Translator.getTranslator().translate("could not be loaded.");
//                    Toast.makeText(ModSynthMidiService.this.getApplication(), "ModSynth: "+msg,
//                            Toast.LENGTH_LONG).show();
                } else {
                    try {
                        synth.setInstrument(sound);
                    } catch (OutOfMemoryError e) {
                        String msg = soundName + " " + Translator.getTranslator().translate("out of memory.");
//                        Toast.makeText(ModSynthMidiService.this.getApplication(), "ModSynth: "+msg,
//                                Toast.LENGTH_LONG).show();
                    }
                }

//            }
//        };
//        thread.start();
        System.out.println("<<ModSynthMidiService.loadInstrument: " + soundName);
    }

}