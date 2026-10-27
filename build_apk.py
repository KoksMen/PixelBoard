import subprocess
import glob
import os
import shutil
import zipfile

java_exe = r"tools\jdk-21.0.5+11\bin\java.exe"
apktool_jar = r"tools\apktool.jar"
android_jar = r"C:\Users\rinsh\AppData\Local\Android\Sdk\platforms\android-36\android.jar"
d8_bat = r"C:\Users\rinsh\AppData\Local\Android\Sdk\build-tools\35.0.0\d8.bat"
zipalign_exe = r"C:\Users\rinsh\AppData\Local\Android\Sdk\build-tools\35.0.0\zipalign.exe"
apksigner_bat = r"C:\Users\rinsh\AppData\Local\Android\Sdk\build-tools\35.0.0\apksigner.bat"
keystore = r"tools\pixelboard.keystore"

src_root = r"pixelboard-patches\extensions\extension\src\main\java"
out_classes = r"_compiled_classes"
out_dex = r"_compiled_dex"
out_apktool = r"_compiled_apktool"
decompiled_root = r"_decompiled"

print("--- 1. Compiling Java extension sources ---")
os.makedirs(out_classes, exist_ok=True)
proofread_files = glob.glob(os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "proofread", "*.java"))
autofill_files = glob.glob(os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "autofill", "*.java"))
spacebar_files = glob.glob(os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "spacebar", "*.java"))
writingtools_files = [
    os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "writingtools", "GboardAiWritingToolsDeviceSupport.java"),
    os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "writingtools", "GboardAiWritingToolsSettings.java"),
    os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "writingtools", "GboardAiWritingToolsRuntime.java"),
]

files = proofread_files + autofill_files + spacebar_files + writingtools_files
print(f"Found {len(files)} files to compile.")

cp = f"{android_jar};{out_classes};{src_root}"
cmd_javac = ["javac", "-encoding", "UTF-8", "-source", "8", "-target", "8", "-cp", cp, "-sourcepath", src_root, "-d", out_classes] + files
res = subprocess.run(cmd_javac, capture_output=True, text=True)
if res.returncode != 0:
    print("javac FAILED:\n", res.stderr)
    exit(1)
print("javac succeeded!")

print("--- 2. Converting classes to DEX with d8 ---")
if os.path.exists(out_dex):
    shutil.rmtree(out_dex)
os.makedirs(out_dex, exist_ok=True)

class_files = glob.glob(os.path.join(out_classes, "**", "*.class"), recursive=True)
cmd_d8 = [d8_bat, "--min-api", "26", "--lib", android_jar, "--output", out_dex] + class_files
res = subprocess.run(cmd_d8, capture_output=True, text=True)
if res.returncode != 0:
    print("d8 FAILED:\n", res.stderr)
    exit(1)
print("d8 succeeded!")

print("--- 3. Disassembling DEX to Smali with Apktool ---")
dex_file = os.path.join(out_dex, "classes.dex")
dex_zip = os.path.join(out_dex, "classes.zip")
with zipfile.ZipFile(dex_zip, "w") as z:
    z.write(dex_file, "classes.dex")

if os.path.exists(out_apktool):
    shutil.rmtree(out_apktool)

cmd_apktool_d = [java_exe, "-jar", apktool_jar, "d", "-f", "-r", dex_zip, "-o", out_apktool]
res = subprocess.run(cmd_apktool_d, capture_output=True, text=True)
if res.returncode != 0:
    print("apktool d FAILED:\n", res.stderr)
    exit(1)
print("apktool d succeeded!")

print("--- 4. Copying Smali into _decompiled ---")
packages = ["proofread", "autofill", "spacebar", "writingtools"]
for pkg in packages:
    src_dir = os.path.join(out_apktool, "smali", "com", "akshaykadam", "pixelboard", "extension", pkg)
    dst_dir = os.path.join(decompiled_root, "smali", "com", "akshaykadam", "pixelboard", "extension", pkg)
    if os.path.exists(src_dir):
        for f in os.listdir(src_dir):
            s_file = os.path.join(src_dir, f)
            d_file = os.path.join(dst_dir, f)
            if os.path.isfile(s_file):
                shutil.copy2(s_file, d_file)
print("Smali files copied successfully!")

print("--- 5. Running patch_smali.py ---")
res = subprocess.run(["python", "patch_smali.py"], capture_output=True, text=True)
print(res.stdout)
if res.returncode != 0:
    print("patch_smali.py FAILED:\n", res.stderr)
    exit(1)

print("--- 6. Rebuilding APK with Apktool ---")
cmd_rebuild = [java_exe, "-jar", apktool_jar, "b", decompiled_root, "-o", "_rebuilt.apk"]
res = subprocess.run(cmd_rebuild, capture_output=True, text=True)
if res.returncode != 0:
    print("apktool b FAILED:\n", res.stderr)
    exit(1)
print("apktool b succeeded!")

print("--- 7. Injecting DEX into pristine resources ---")
res = subprocess.run(["python", "inject_dex.py"], capture_output=True, text=True)
print(res.stdout)
if res.returncode != 0:
    print("inject_dex.py FAILED:\n", res.stderr)
    exit(1)

print("--- 8. Zipalign and sign V31 APK ---")
v31_apk = r"output\PixelBoard-18.4.1-V31.apk"
final_apk = r"output\PixelBoard.apk"
raw_apk = r"output\PixelBoard_raw.apk"

cmd_zipalign = [zipalign_exe, "-f", "-p", "4", raw_apk, v31_apk]
res = subprocess.run(cmd_zipalign, capture_output=True, text=True)
if res.returncode != 0:
    print("zipalign FAILED:\n", res.stderr)
    exit(1)

cmd_sign = [apksigner_bat, "sign", "--ks", keystore, "--ks-pass", "pass:pixelboard", v31_apk]
res = subprocess.run(cmd_sign, capture_output=True, text=True)
if res.returncode != 0:
    print("apksigner sign FAILED:\n", res.stderr)
    exit(1)

cmd_verify = [apksigner_bat, "verify", v31_apk]
res = subprocess.run(cmd_verify, capture_output=True, text=True)
if res.returncode != 0:
    print("apksigner verify FAILED:\n", res.stderr)
    exit(1)

shutil.copy2(v31_apk, final_apk)
print(f"Build complete! Successfully produced and signed {v31_apk} and {final_apk}")
