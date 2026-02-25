# BootSequence Asset Pack

The full boot/splash image pack is **not stored in Git**.

## Why
These large image assets were previously tracked via Git LFS, which can make regular clones unreliable when LFS quota is exhausted.
To keep `git clone` reliable, this repository now ships without these binary files.

## Download the asset pack
1. Open the project Releases page:
   - `https://github.com/LuxZen-Tao/JavaBarGUI/releases`
2. Download the latest boot sequence asset archive (for example `BootSequence.zip`).
3. Extract it into the repository root so files land at:
   - `Art/BootSequence/...`

## Expected folder layout
After extraction, this folder should contain content like:
- `Art/BootSequence/Logos/StudioLogo.png`
- `Art/BootSequence/Splash/GameBootMenu.png`
- `Art/BootSequence/Photos/*.png`

If no archive exists yet, publish one on Releases and upload the boot image pack there.
