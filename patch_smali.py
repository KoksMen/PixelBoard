import os

# 1. Create GboardSpacebarToggleAction.smali with try/catch protection
toggle_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction;
.super Ljava/lang/Object;
.source "GboardSpacebarToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSettings;->writeEnabled(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardSpacebar"
    const-string v2, "Failed to toggle spacebar setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

os.makedirs("_decompiled/smali/com/akshaykadam/pixelboard/extension/spacebar", exist_ok=True)
with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(toggle_action_smali)
print("Updated GboardSpacebarToggleAction.smali with try/catch")

# 2. Update GboardPatchesSettingsActivity.smali with literal strings
settings_activity_path = "_decompiled/smali/com/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity.smali"
with open(settings_activity_path, "r", encoding="utf-8") as f:
    content = f.read()

target_existing_swipe = """    # Spacebar swipe toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    sget v2, Lcom/akshaykadam/pixelboard/extension/R$string;->gboard_patches_swipe_space_switch_language_title:I

    invoke-static {v0, v2}, Lcom/akshaykadam/pixelboard/extension/settings/GboardSettingsText;->get(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v17

    sget v2, Lcom/akshaykadam/pixelboard/extension/R$string;->gboard_patches_swipe_space_switch_language_summary:I

    invoke-static {v0, v2}, Lcom/akshaykadam/pixelboard/extension/settings/GboardSettingsText;->get(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v18"""

replacement_bulletproof_swipe = """    # Spacebar swipe toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\\u0421\\u0432\\u0430\\u0439\\u043f \\u043f\\u043e \\u043f\\u0440\\u043e\\u0431\\u0435\\u043b\\u0443 \\u0434\\u043b\\u044f \\u0441\\u043c\\u0435\\u043d\\u044b \\u044f\\u0437\\u044b\\u043a\\u0430"

    const-string v18, "\\u041f\\u0440\\u043e\\u0432\\u0435\\u0434\\u0438\\u0442\\u0435 \\u0432\\u043b\\u0435\\u0432\\u043e \\u0438\\u043b\\u0438 \\u0432\\u043f\\u0440\\u0430\\u0432\\u043e \\u043f\\u043e \\u043f\\u0440\\u043e\\u0431\\u0435\\u043b\\u0443, \\u0447\\u0442\\u043e\\u0431\\u044b \\u0431\\u044b\\u0441\\u0442\\u0440\\u043e \\u043f\\u0435\\u0440\\u0435\\u043a\\u043b\\u044e\\u0447\\u0438\\u0442\\u044c \\u044f\\u0437\\u044b\\u043a \\u0432\\u0432\\u043e\\u0434\\u0430\""""

content_norm = content.replace("\r\n", "\n")
target_norm = target_existing_swipe.replace("\r\n", "\n")

if target_norm in content_norm:
    content_norm = content_norm.replace(target_norm, replacement_bulletproof_swipe.replace("\r\n", "\n"), 1)
    with open(settings_activity_path, "w", encoding="utf-8") as f:
        f.write(content_norm)
    print("Replaced GboardSettingsText lookups with direct literal strings in GboardPatchesSettingsActivity")
else:
    print("Direct literal strings already present or target not found")

# 3. Clean hook for SoftKeyboardView.smali (no malformed catch block; handleSoftKeyboardViewTouchEvent is protected in Java)
skv_path = "_decompiled/smali_classes2/com/google/android/libraries/inputmethod/widgets/SoftKeyboardView.smali"
with open(skv_path, "r", encoding="utf-8") as f:
    skv_content = f.read()

skv_norm = skv_content.replace("\r\n", "\n")
flawed_skv_hook = """.method public final dispatchTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 4

    :try_start_spacebar_hook
    invoke-static {p0, p1}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSwipeRuntime;->handleSoftKeyboardViewTouchEvent(Landroid/view/View;Landroid/view/MotionEvent;)Z
    move-result v0
    if-eqz v0, :cond_spacebar_softkeyboard_normal

    const/4 v0, 0x1
    return v0
    :try_end_spacebar_hook
    .catchall {:try_start_spacebar_hook .. :try_end_spacebar_hook} :catchall_spacebar_hook

    :catchall_spacebar_hook
    move-exception v0

    :cond_spacebar_softkeyboard_normal"""

clean_skv_hook = """.method public final dispatchTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 4

    invoke-static {p0, p1}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSwipeRuntime;->handleSoftKeyboardViewTouchEvent(Landroid/view/View;Landroid/view/MotionEvent;)Z
    move-result v0
    if-eqz v0, :cond_spacebar_softkeyboard_normal

    const/4 v0, 0x1
    return v0

    :cond_spacebar_softkeyboard_normal"""

if flawed_skv_hook in skv_norm:
    skv_norm = skv_norm.replace(flawed_skv_hook, clean_skv_hook, 1)
    with open(skv_path, "w", encoding="utf-8") as f:
        f.write(skv_norm)
    print("Restored clean, verified hook in SoftKeyboardView.dispatchTouchEvent")
else:
    print("SoftKeyboardView hook already in clean state")

# 4. Clean hook for LatinIME.smali (no malformed catch block; registerInputMethodService is protected in Java)
lime_path = "_decompiled/smali_classes2/com/android/inputmethod/latin/LatinIME.smali"
with open(lime_path, "r", encoding="utf-8") as f:
    lime_content = f.read()

lime_norm = lime_content.replace("\r\n", "\n")
flawed_lime = """invoke-direct {p0}, Lesi;-><init>()V

    :try_start_spacebar_ims
    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSwipeRuntime;->registerInputMethodService(Landroid/inputmethodservice/InputMethodService;)V
    :try_end_spacebar_ims
    .catchall {:try_start_spacebar_ims .. :try_end_spacebar_ims} :catchall_spacebar_ims

    :catchall_spacebar_ims
    move-exception v0"""

clean_lime = """invoke-direct {p0}, Lesi;-><init>()V

    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSwipeRuntime;->registerInputMethodService(Landroid/inputmethodservice/InputMethodService;)V"""

if flawed_lime in lime_norm:
    lime_norm = lime_norm.replace(flawed_lime, clean_lime, 1)
    with open(lime_path, "w", encoding="utf-8") as f:
        f.write(lime_norm)
    print("Restored clean, verified hook in LatinIME constructor")
else:
    print("LatinIME hook already in clean state")
