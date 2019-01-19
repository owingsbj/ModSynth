# ModSynth

A modular synthesizer for Android.  See https://play.google.com/store/apps/details?id=com.gallantrealm.modsynth for the installable.  If you'd like to view or create an issue for a problem or feature request see the list at https://github.com/owingsbj/ModSynth/issues.

This synthesizer is based on the [MySynth](https://github.com/owingsbj/MySynth) synthesizer engine.   It also uses widgets that I've created or obtained and placed into the [MyAndroid](https://github.com/owingsbj/MyAndroid) repository.

The source for ModSynth is available here for your examination and suggestion for improvement.  You could also create derived works of course, as it is allowed by the license.  However, I wouldn't suggest making another duplicate or slightly modified ModSynth application and releasing it on Google Play, since

1. There already is one.
2. You won't make much money at it.  I never did nor did I ever intend to.
3. You could more easily assist in the development of the existing ModSynth.

The Android project files are still eclipse-based.  I haven't had luck converting to AndroidStudio for this project so am still using eclipse (with the AndMore plugin).  I do have issues when using eclipse (even when using AndMore) in that it runs into dx loading issues.  I've worked around it by doing the following (from https://stackoverflow.com/questions/43009679/unknown-error-unable-to-build-the-file-dx-jar-was-not-loaded-from-the-sdk-fold/43040723):

```
Eclipse ADT no more support. So Google break backward compatibility with remove two classes from dx.jar.
You can easy fix it.

- Go to your sdk folder. Navigate to dx.jar from latest build-tools.
- For example build-tools\28.0.3\lib
- Open dx.jar in any zip archiver.  I use WinRAR.
- Navigate to path com\android\dx\command inside archive.
- Here you not see files DxConsole$1.class and DxConsole.class.
- Now navigate to dx.jar for 25.0.3 or before.
- Again navigate to com\android\dx\command inside this archive.
- Here you see files DxConsole$1.class and DxConsole.class. Copy it from old dx.jar
  to new dx.jar. I just drop its from one WinRAR window to another.

All done. Now you can use new dx.jar with Eclipse ADT.

This solution better from replace dx.jar, because you can use new version of the dx.jar.

You need do this steps on every update build-tools.
```

Feel free to email me at bj@gallantrealm.com if you have questions about the source.  I intend to improve its comments to make it easier to understand.
