# External Assets

## Music assets are distributed outside git
`MusicProfiles/` audio files are intentionally excluded from version control to keep repository clones lightweight and independent from Git LFS quotas.

## How to install music assets
1. Go to GitHub Releases:
   - `https://github.com/LuxZen-Tao/JavaBarGUI/releases`
2. Download the latest audio asset bundle (for example `MusicProfiles.zip`).
3. Extract into the repository root.

Resulting path should be:
- `MusicProfiles/<audio files and subfolders>`

## Notes for contributors
- Do **not** commit large audio files into git.
- Keep `MusicProfiles/README.md` tracked so users know how to fetch assets.
- If needed, publish updated music bundles via Releases rather than adding binaries to the repository.


## Boot sequence images are distributed outside git
`Art/BootSequence/` image assets are intentionally excluded from version control to keep repository clones lightweight and independent from Git LFS quotas.

## How to install boot sequence images
1. Go to GitHub Releases:
   - `https://github.com/LuxZen-Tao/JavaBarGUI/releases`
2. Download the latest boot sequence asset bundle (for example `BootSequence.zip`).
3. Extract into the repository root.

Resulting path should be:
- `Art/BootSequence/<image files and subfolders>`

## Notes for contributors (boot sequence assets)
- Do **not** commit large image files into git.
- Keep `Art/BootSequence/README.md` tracked so users know how to fetch assets.
- Publish updated image bundles via Releases rather than adding binaries to the repository.
