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

# 1.1 Create GboardProofreadToggleAction.smali with try/catch protection
proofread_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;
.super Ljava/lang/Object;
.source "GboardProofreadToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeEnabled(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardProofread"
    const-string v2, "Failed to toggle proofread setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

os.makedirs("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread", exist_ok=True)
with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(proofread_action_smali)
print("Updated GboardProofreadToggleAction.smali with try/catch")

# 2. Update GboardPatchesSettingsActivity.smali with literal strings and proofread toggle row
settings_activity_path = "_decompiled/smali/com/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity.smali"
with open(settings_activity_path, "r", encoding="utf-8") as f:
    content = f.read()

content_norm = content.replace("\r\n", "\n")

proofread_row_smali = """
    # Proofread toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\\u041f\\u0440\\u043e\\u0432\\u0435\\u0440\\u043a\\u0430 \\u0442\\u0435\\u043a\\u0441\\u0442\\u0430 (\\u0441 \\u0413\\u0438\\u0442-\\u0434\\u0438\\u0444\\u0444\\u043e\\u043c)"

    const-string v18, "\\u041f\\u0440\\u043e\\u0432\\u0435\\u0440\\u043a\\u0430 \\u0438 \\u0438\\u0441\\u043f\\u0440\\u0430\\u0432\\u043b\\u0435\\u043d\\u0438\\u0435 \\u043e\\u0448\\u0438\\u0431\\u043e\\u043a \\u0447\\u0435\\u0440\\u0435\\u0437 \\u042f\\u043d\\u0434\\u0435\\u043a\\u0441 \\u0441 \\u043f\\u043e\\u0434\\u0441\\u0432\\u0435\\u0442\\u043a\\u043e\\u0439 \\u043a\\u0440\\u0430\\u0441\\u043d\\u044b\\u043c \\u0438 \\u0437\\u0435\\u043b\\u0435\\u043d\\u044b\\u043c. \\u041a\\u043d\\u043e\\u043f\\u043a\\u0430 \\u2728 \\u0438\\u043b\\u0438 \\u0441\\u0432\\u0430\\u0439\\u043f \\u0432\\u0432\\u0435\\u0440\\u0445 \\u043f\\u043e \\u043f\\u0440\\u043e\\u0431\\u0435\\u043b\\u0443."

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readEnabled(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"""

spacebar_row_anchor = """    new-instance v2, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"""

if "GboardProofreadToggleAction" not in content_norm and spacebar_row_anchor in content_norm:
    content_norm = content_norm.replace(spacebar_row_anchor, spacebar_row_anchor + "\n" + proofread_row_smali, 1)
    with open(settings_activity_path, "w", encoding="utf-8") as f:
        f.write(content_norm)
    print("Added GboardProofreadToggleAction row to GboardPatchesSettingsActivity")
else:
    print("GboardProofreadToggleAction already present or anchor not found")

# 3. Clean hook for SoftKeyboardView.smali (no malformed catch block; handleSoftKeyboardViewTouchEvent is protected in Java)
skv_path = "_decompiled/smali_classes2/com/google/android/libraries/inputmethod/widgets/SoftKeyboardView.smali"
with open(skv_path, "r", encoding="utf-8") as f:
    skv_content = f.read()

skv_norm = skv_content.replace("\r\n", "\n")
clean_skv_hook = """.method public final dispatchTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 4

    invoke-static {p0, p1}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSwipeRuntime;->handleSoftKeyboardViewTouchEvent(Landroid/view/View;Landroid/view/MotionEvent;)Z
    move-result v0
    if-eqz v0, :cond_spacebar_softkeyboard_normal

    const/4 v0, 0x1
    return v0

    :cond_spacebar_softkeyboard_normal"""

if clean_skv_hook in skv_norm:
    print("SoftKeyboardView hook already in clean state")
else:
    print("Warning: SoftKeyboardView hook not matched")

# 4. Clean hook for LatinIME.smali (no malformed catch block; registerInputMethodService is protected in Java)
lime_path = "_decompiled/smali_classes2/com/android/inputmethod/latin/LatinIME.smali"
with open(lime_path, "r", encoding="utf-8") as f:
    lime_content = f.read()

lime_norm = lime_content.replace("\r\n", "\n")
clean_lime = """invoke-direct {p0}, Lesi;-><init>()V

    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/spacebar/GboardSpacebarSwipeRuntime;->registerInputMethodService(Landroid/inputmethodservice/InputMethodService;)V"""

if clean_lime in lime_norm:
    print("LatinIME hook already in clean state")
else:
    print("Warning: LatinIME hook not matched")
