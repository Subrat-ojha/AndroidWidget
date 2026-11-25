# Androiid widget

A self-contained 3D beach-cat widget. Tap to pet the cat, swipe sideways to change the scene (seven scenes; they also auto-advance). Two of the scenes are triggered automatically by music and calls: the home-screen widget and the full app both switch to headphones while music is playing and to the phone scene while a call is active, then return to the normal loop. Android widgets are static snapshots, so the widget swaps a new still every 10 seconds instead of running the WebGL animation.

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
