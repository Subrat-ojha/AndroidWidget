# Androiid widget

A self-contained 3D beach-cat widget. Tap to pet the cat, swipe sideways to change the scene (seven scenes; they also auto-advance). Two of the scenes are triggered automatically by the Android app: headphones while music is playing, and a phone call while a call is active.

## Deploy

This is a static site — no build step. Connect the repo to Netlify with:

- **Build command:** *(empty)*
- **Publish directory:** `.`

`netlify.toml` already sets the publish directory, so the defaults work too.

To preview locally, serve the folder over HTTP, e.g.:

```sh
python3 -m http.server 8080
```

Then open <http://localhost:8080>.

Three.js r128 is vendored in `vendor/three.min.js`, so there is no runtime CDN dependency.
