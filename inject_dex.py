import zipfile
import os

src_apk = "tools/clean_PixelBoard.apk"
rebuilt_apk = "_rebuilt.apk"
out_apk = "output/PixelBoard_raw.apk"

print("Reading all dex files from rebuilt apk...")
rebuilt_dex = {}
with zipfile.ZipFile(rebuilt_apk, "r") as z_rebuilt:
    for name in z_rebuilt.namelist():
        if name.endswith(".dex"):
            data = z_rebuilt.read(name)
            rebuilt_dex[name] = data
            print(f"Loaded {name}: {len(data)} bytes")

print("Injecting into clean base APK while preserving all original resources...")
with zipfile.ZipFile(src_apk, "r") as z_in, zipfile.ZipFile(out_apk, "w", compression=zipfile.ZIP_DEFLATED) as z_out:
    for item in z_in.infolist():
        # Strip old signature files
        if item.filename.startswith("META-INF/") and (
            item.filename.endswith(".SF")
            or item.filename.endswith(".RSA")
            or item.filename.endswith(".DSA")
            or item.filename.endswith(".EC")
            or item.filename.endswith("MANIFEST.MF")
        ):
            continue
        
        # Replace dex files with rebuilt ones
        if item.filename in rebuilt_dex:
            z_out.writestr(item, rebuilt_dex[item.filename])
        else:
            # Preserve exact data for all other files without recompressing
            data = z_in.read(item.filename)
            z_out.writestr(item, data)

print("APK successfully constructed with all patched dex and pristine resources!")
