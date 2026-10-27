import subprocess
import glob
import os

android_jar = r"C:\Users\rinsh\AppData\Local\Android\Sdk\platforms\android-36\android.jar"
junit_jar = r"C:\Users\rinsh\.gradle\caches\modules-2\files-2.1\junit\junit\4.13.2\8ac9e16d933b6fb43bc7f576336b8f4d7eb5ba12\junit-4.13.2.jar"
hamcrest_jar = r"C:\Users\rinsh\.gradle\caches\modules-2\files-2.1\org.hamcrest\hamcrest-core\1.3\42a25dc3219429f0e5d060061f71acb49bf010a0\hamcrest-core-1.3.jar"
src_root = r"c:\Users\rinsh\Documents\antigravity\quirky-planck\pixelboard-patches\extensions\extension\src\main\java"
test_src = r"c:\Users\rinsh\Documents\antigravity\quirky-planck\pixelboard-patches\extensions\extension\src\test\java\com\akshaykadam\pixelboard\extension\proofread\GboardProofreadServiceTest.java"
out_dir = r"c:\Users\rinsh\Documents\antigravity\quirky-planck\_compiled_classes"

os.makedirs(out_dir, exist_ok=True)

proofread_files = glob.glob(os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "proofread", "*.java"))
writingtools_files = [
    os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "writingtools", "GboardAiWritingToolsDeviceSupport.java"),
    os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "writingtools", "GboardAiWritingToolsSettings.java"),
    os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "writingtools", "GboardAiWritingToolsRuntime.java"),
]
spacebar_files = glob.glob(os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "spacebar", "*.java"))
autofill_files = glob.glob(os.path.join(src_root, "com", "akshaykadam", "pixelboard", "extension", "autofill", "*.java"))

files = proofread_files + writingtools_files + spacebar_files + autofill_files
cp = f"{out_dir};{android_jar};{junit_jar};{hamcrest_jar};{src_root}"

cmd_compile = ["javac", "-encoding", "UTF-8", "-source", "8", "-target", "8", "-cp", cp, "-sourcepath", src_root, "-d", out_dir] + files + [test_src]
print("Compiling...")
res = subprocess.run(cmd_compile, capture_output=True, text=True)
if res.returncode != 0:
    print("Compile error:\n", res.stderr)
    exit(1)
print("Compile success!")

java_exe = r"tools\jdk-21.0.5+11\bin\java.exe"
cmd_test = [java_exe, "-Dfile.encoding=UTF-8", "-cp", cp, "org.junit.runner.JUnitCore", "com.akshaykadam.pixelboard.extension.proofread.GboardProofreadServiceTest"]
print("Running JUnit...")
res_test = subprocess.run(cmd_test, capture_output=True, text=True, encoding="utf-8")
print(res_test.stdout)
if res_test.stderr:
    print(res_test.stderr)
exit(res_test.returncode)
