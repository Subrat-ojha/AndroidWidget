# Android build

`AndroiidWidget.apk` at the repository root is the signed release build. It is a
full-screen WebView wrapping the same widget that is deployed on Netlify.

- Package: `com.androiid.widget`
- Min SDK: 21 (Android 5.0), target SDK 34
- Signature: v1 + v2 + v3
- Scenes: nap, purr, meal, walk, night, music (headphones), call (phone)
- A real home-screen AppWidget is included: resizable, tap the picture to cycle
  scenes and tap the bottom bar to open the full app. It appears in the system
  widget picker as "Beach cat widget".

## Automatic music / call scenes

`MainActivity` exposes a small JavaScript bridge (`Android.getState()`), polled
by the page. When Android reports `music`, the cat switches to the headphones
scene (sitting, facing the viewer, next to a radio); when it reports `call`, it
switches to the phone scene (sitting, facing the viewer, holding the phone to its
ear). Call takes priority over music. When both end, the widget returns to the
scene that was active before the state change. No permissions are required:
detection uses `AudioManager.isMusicActive()` and `AudioManager.getMode()`.

## Launch robustness

- System-bar hiding is only done after the view is attached and is null-safe.
- WebView creation is wrapped so devices with a missing/broken WebView show a
  message instead of crashing.
- If WebGL is unavailable, the page shows a message instead of failing silently.

On Netlify (no bridge present), all seven scenes are reachable by swiping and
by the auto-rotation.

## Project layout

```
android/app/
  AndroidManifest.xml
  java/com/androiid/widget/MainActivity.java
  res/            theme, label, launcher icon
  assets/www/     index.html + vendor/three.min.js
```

## Layout / responsiveness

The WebView fills the screen, hides system bars (immersive), and the widget is
sized with `min()` plus a JS height fallback so it fits both portrait and
landscape on older WebViews as well as current ones. Rotation is handled without
recreating the activity.

## Rebuilding

The build was produced with the Android SDK command-line tools (build-tools 34,
platform 34), `javac`, `aapt2`, `d8`, `zipalign` and `apksigner`. The signing
keystore is kept locally and is intentionally gitignored.

Steps:

1. `aapt2 compile --dir res -o build/res.zip`
2. `aapt2 link -o build/base.apk -I <android.jar> --manifest AndroidManifest.xml --java build/gen --auto-add-overlay -A assets build/res.zip`
3. `javac -source 8 -target 8 -bootclasspath <android.jar> -d build/classes java/com/androiid/widget/MainActivity.java`
4. `d8 --lib <android.jar> --output build/classes build/classes/com/androiid/widget/MainActivity.class`
5. add `classes.dex` to `base.apk`, `zipalign -p 4`, then `apksigner sign`.
