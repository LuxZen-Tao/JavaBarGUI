# MusicProfiles Asset Pack

The full music/ambience pack is **not stored in Git** anymore.

## Why
Large audio assets previously used Git LFS and could block normal clones when LFS quota was exceeded.
To keep `git clone` reliable, this repository now ships without those binary files.

## Download the asset pack
1. Open the project Releases page (example):
   - `https://github.com/LuxZen-Tao/JavaBarGUI/releases`
2. Download the latest `MusicProfiles.zip` (or similarly named music asset archive).
3. Extract it into the repository root so files land at:
   - `MusicProfiles/...`

## Expected folder layout
After extraction, this folder should contain files like:
- `MusicProfiles/JAZZ_LOUNGE.wav`
- `MusicProfiles/INDIE_ALT/INDIE_ALT1.wav`
- `MusicProfiles/Ambience/ChatterLarge.wav`

If no asset archive is available yet, create a release and upload the music pack there.
