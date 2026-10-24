import zipfile
import os

src_apk = "tools/clean_PixelBoard.apk"
rebuilt_apk = "_rebuilt.apk"
out_apk = "output/PixelBoard_raw.apk"

print("Reading classes.dex and classes2.dex from rebuilt apk...")
with zipfile.ZipFile(rebuilt_apk, "r") as z_rebuilt:
    classes_dex = z_rebuilt.read("classes.dex")
    classes2_dex = z_rebuilt.read("classes2.dex")

print(f"classes.dex size: {len(classes_dex)}")
print(f"classes2.dex size: {len(classes2_dex)}")

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
        
        # Replace classes.dex and classes2.dex
        if item.filename == "classes.dex":
            z_out.writestr(item, classes_dex)
        elif item.filename == "classes2.dex":
            z_out.writestr(item, classes2_dex)
        else:
            # Preserve exact data for all other files without recompressing
            data = z_in.read(item.filename)
            z_out.writestr(item, data)

print("APK successfully constructed with patched dex and pristine resources!")
