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

# 1.2 Create GboardProofreadAlwaysVisibleToggleAction.smali
proofread_always_visible_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction;
.super Ljava/lang/Object;
.source "GboardProofreadAlwaysVisibleToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeAlwaysVisible(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardProofread"
    const-string v2, "Failed to toggle proofread always visible setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(proofread_always_visible_action_smali)
print("Updated GboardProofreadAlwaysVisibleToggleAction.smali with try/catch")

# 1.3 Create GboardAutofillToggleAction.smali
autofill_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction;
.super Ljava/lang/Object;
.source "GboardAutofillToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillSettings;->writeEnabled(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardAutofill"
    const-string v2, "Failed to toggle autofill setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

os.makedirs("_decompiled/smali/com/akshaykadam/pixelboard/extension/autofill", exist_ok=True)
with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(autofill_action_smali)
print("Updated GboardAutofillToggleAction.smali with try/catch")

# 1.4 Create GboardToolbarPositionToggleAction.smali
toolbar_pos_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction;
.super Ljava/lang/Object;
.source "GboardToolbarPositionToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writePositionLeft(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardProofread"
    const-string v2, "Failed to toggle toolbar position setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(toolbar_pos_action_smali)
print("Created GboardToolbarPositionToggleAction.smali")

# 1.5 Create GboardToolbarOrderToggleAction.smali
toolbar_order_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction;
.super Ljava/lang/Object;
.source "GboardToolbarOrderToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeOrderSparkleFirst(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardProofread"
    const-string v2, "Failed to toggle toolbar order setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(toolbar_order_action_smali)
print("Created GboardToolbarOrderToggleAction.smali")

# 1.6 Hide Stickers Action
hide_stickers_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;
.super Ljava/lang/Object;
.source "GboardHideStickersToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeHideStickers(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardToolbar"
    const-string v2, "Failed to toggle hide stickers setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(hide_stickers_action_smali)

# 1.7 Hide GIF Action
hide_gif_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;
.super Ljava/lang/Object;
.source "GboardHideGifToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeHideGif(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardToolbar"
    const-string v2, "Failed to toggle hide gif setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(hide_gif_action_smali)

# 1.8 Hide Themes Action
hide_themes_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;
.super Ljava/lang/Object;
.source "GboardHideThemesToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeHideThemes(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardToolbar"
    const-string v2, "Failed to toggle hide themes setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(hide_themes_action_smali)

# 1.9 Hide Translate Action
hide_translate_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;
.super Ljava/lang/Object;
.source "GboardHideTranslateToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeHideTranslate(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardToolbar"
    const-string v2, "Failed to toggle hide translate setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(hide_translate_action_smali)

# 1.10 Hide Clipboard Action
hide_clipboard_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;
.super Ljava/lang/Object;
.source "GboardHideClipboardToggleAction.java"

# interfaces
.implements Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final accept(Z)V
    .registers 5

    :try_start_0
    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;->preferences:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    invoke-static {v0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->writeHideClipboard(Landroid/content/SharedPreferences;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catch_0

    :cond_0
    return-void

    :catch_0
    move-exception v0
    const-string v1, "PixelBoardToolbar"
    const-string v2, "Failed to toggle hide clipboard setting"
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    return-void
.end method
"""

with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction.smali", "w", encoding="utf-8") as f:
    f.write(hide_clipboard_action_smali)
print("Created hide toolbar buttons toggle actions")


# 2. Update GboardPatchesSettingsActivity.smali with literal strings and toggle rows
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

extra_rows_smali = """
    # Proofread always visible toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\\u0411\\u044b\\u0441\\u0442\\u0440\\u044b\\u0439 \\u0432\\u044b\\u0437\\u043e\\u0432 \\u043f\\u0440\\u043e\\u0432\\u0435\\u0440\\u043a\\u0438 \\u0442\\u0435\\u043a\\u0441\\u0442\\u0430"

    const-string v18, "\\u041a\\u043d\\u043e\\u043f\\u043a\\u0430 \\u2728 \\u0432\\u0441\\u0435\\u0433\\u0434\\u0430 \\u043d\\u0430 \\u043f\\u0430\\u043d\\u0435\\u043b\\u0438 \\u043a\\u043b\\u0430\\u0432\\u0438\\u0430\\u0442\\u0443\\u0440\\u044b (\\u0432\\u044b\\u043a\\u043b: \\u0442\\u043e\\u043b\\u044c\\u043a\\u043e \\u043f\\u043e \\u043a\\u0430\\u0441\\u0430\\u043d\\u0438\\u044e \\u043f\\u0440\\u043e\\u0431\\u0435\\u043b\\u0430/\\u0436\\u0435\\u0441\\u0442\\u0443)"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readAlwaysVisible(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadAlwaysVisibleToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Autofill toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\\u041a\\u043d\\u043e\\u043f\\u043a\\u0430 \\u0430\\u0432\\u0442\\u043e\\u0437\\u0430\\u043f\\u043e\\u043b\\u043d\\u0435\\u043d\\u0438\\u044f (\\ud83d\\udd11)"

    const-string v18, "\\u041e\\u0442\\u043e\\u0431\\u0440\\u0430\\u0436\\u0430\\u0442\\u044c \\u043a\\u043d\\u043e\\u043f\\u043a\\u0443 \\ud83d\\udd11 \\u043d\\u0430 \\u043f\\u0430\\u043d\\u0435\\u043b\\u0438 \\u043a\\u043b\\u0430\\u0432\\u0438\\u0430\\u0442\\u0443\\u0440\\u044b \\u0434\\u043b\\u044f \\u0432\\u044b\\u0437\\u043e\\u0432\\u0430 \\u0430\\u0432\\u0442\\u043e\\u0437\\u0430\\u043f\\u043e\\u043b\\u043d\\u0435\\u043d\\u0438\\u044f \\u0432 \\u043f\\u043e\\u043b\\u044f\\u0445 \\u0432\\u0432\\u043e\\u0434\\u0430"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillSettings;->readEnabled(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/autofill/GboardAutofillToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

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

proofread_row_anchor = """    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"""

if "GboardProofreadToggleAction" not in content_norm and spacebar_row_anchor in content_norm:
    content_norm = content_norm.replace(spacebar_row_anchor, spacebar_row_anchor + "\n" + proofread_row_smali, 1)
    with open(settings_activity_path, "w", encoding="utf-8") as f:
        f.write(content_norm)
    print("Added GboardProofreadToggleAction row to GboardPatchesSettingsActivity")

if "GboardAutofillToggleAction" not in content_norm:
    idx_proofread = content_norm.find("GboardProofreadToggleAction")
    if idx_proofread != -1:
        target_add = "invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"
        idx_add = content_norm.find(target_add, idx_proofread)
        if idx_add != -1:
            insert_pos = idx_add + len(target_add)
            content_norm = content_norm[:insert_pos] + "\n" + extra_rows_smali + content_norm[insert_pos:]
            with open(settings_activity_path, "w", encoding="utf-8") as f:
                f.write(content_norm)
            print("Added GboardProofreadAlwaysVisible and GboardAutofill rows to GboardPatchesSettingsActivity")
else:
    print("Extra rows already present or anchor not found")

position_and_order_rows_smali = """
    # Toolbar position toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\\u041a\\u043d\\u043e\\u043f\\u043a\\u0438 \\u043f\\u0430\\u043d\\u0435\\u043b\\u0438: \\u0441\\u043b\\u0435\\u0432\\u0430 (\\u0443 \\u043c\\u0435\\u043d\\u044e ::)"

    const-string v18, "\\u0415\\u0441\\u043b\\u0438 \\u0432\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u043e, \\u043a\\u043d\\u043e\\u043f\\u043a\\u0438 \\ud83d\\udd11 \\u0438 \\u2728 \\u0431\\u0443\\u0434\\u0443\\u0442 \\u0441\\u043b\\u0435\\u0432\\u0430 \\u0440\\u044f\\u0434\\u043e\\u043c \\u0441 \\u043c\\u0435\\u043d\\u044e ::, \\u0438\\u043d\\u0430\\u0447\\u0435 \\u2014 \\u0441\\u043f\\u0440\\u0430\\u0432\\u0430 \\u043f\\u0435\\u0440\\u0435\\u0434 \\u043c\\u0438\\u043a\\u0440\\u043e\\u0444\\u043e\\u043d\\u043e\\u043c"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readPositionLeft(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarPositionToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Toolbar order toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\\u041f\\u043e\\u0440\\u044f\\u0434\\u043e\\u043a \\u043a\\u043d\\u043e\\u043f\\u043e\\u043a: [\\u2728] \\u0437\\u0430\\u0442\\u0435\\u043c [\\ud83d\\udd11]"

    const-string v18, "\\u0415\\u0441\\u043b\\u0438 \\u0432\\u043a\\u043b\\u044e\\u0447\\u0435\\u043d\\u043e \\u2014 [\\u2728][\\ud83d\\udd11], \\u0438\\u043d\\u0430\\u0447\\u0435 \\u2014 [\\ud83d\\udd11][\\u2728]"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readOrderSparkleFirst(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardToolbarOrderToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    # Hide stickers toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u0441\u0442\u0438\u043a\u0435\u0440\u044b \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u0441\u0442\u0438\u043a\u0435\u0440\u043e\u0432 \u0441 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u043f\u0430\u043d\u0435\u043b\u0438 Gboard"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideStickers(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide GIF toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c GIF \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u043f\u043e\u0438\u0441\u043a\u0430 GIF \u0441 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u043f\u0430\u043d\u0435\u043b\u0438 Gboard"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideGif(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide themes toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u0442\u0435\u043c\u044b \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u043f\u0430\u043b\u0438\u0442\u0440\u044b \u0442\u0435\u043c \u0441 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u043f\u0430\u043d\u0435\u043b\u0438 Gboard"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideThemes(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide translate toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0447\u0438\u043a \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 Google \u041f\u0435\u0440\u0435\u0432\u043e\u0434\u0447\u0438\u043a\u0430 \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideTranslate(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide clipboard toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u0431\u0443\u0444\u0435\u0440 \u043e\u0431\u043c\u0435\u043d\u0430 \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u0431\u0443\u0444\u0435\u0440\u0430 \u043e\u0431\u043c\u0435\u043d\u0430 \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideClipboard(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"""

if "GboardHideStickersToggleAction" not in content_norm:
    # If GboardToolbarPositionToggleAction was already inserted, find it and insert filter rows after it
    idx_order = content_norm.find("GboardToolbarOrderToggleAction")
    if idx_order != -1:
        target_add = "invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"
        idx_add = content_norm.find(target_add, idx_order)
        if idx_add != -1:
            insert_pos = idx_add + len(target_add)
            filter_rows_only = """# Hide stickers toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u0441\u0442\u0438\u043a\u0435\u0440\u044b \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u0441\u0442\u0438\u043a\u0435\u0440\u043e\u0432 \u0441 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u043f\u0430\u043d\u0435\u043b\u0438 Gboard"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideStickers(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideStickersToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide GIF toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c GIF \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u043f\u043e\u0438\u0441\u043a\u0430 GIF \u0441 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u043f\u0430\u043d\u0435\u043b\u0438 Gboard"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideGif(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideGifToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide themes toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u0442\u0435\u043c\u044b \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u043f\u0430\u043b\u0438\u0442\u0440\u044b \u0442\u0435\u043c \u0441 \u0432\u0435\u0440\u0445\u043d\u0435\u0439 \u043f\u0430\u043d\u0435\u043b\u0438 Gboard"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideThemes(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideThemesToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide translate toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u043f\u0435\u0440\u0435\u0432\u043e\u0434\u0447\u0438\u043a \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 Google \u041f\u0435\u0440\u0435\u0432\u043e\u0434\u0447\u0438\u043a\u0430 \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideTranslate(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideTranslateToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    # Hide clipboard toggle row
    new-instance v16, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;

    const-string v17, "\u0421\u043a\u0440\u044b\u0442\u044c \u0431\u0443\u0444\u0435\u0440 \u043e\u0431\u043c\u0435\u043d\u0430 \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const-string v18, "\u0423\u0431\u0438\u0440\u0430\u0435\u0442 \u0438\u043a\u043e\u043d\u043a\u0443 \u0431\u0443\u0444\u0435\u0440\u0430 \u043e\u0431\u043c\u0435\u043d\u0430 \u0441 \u043f\u0430\u043d\u0435\u043b\u0438"

    const/16 v19, 0x1

    invoke-static {v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadSettings;->readHideClipboard(Landroid/content/SharedPreferences;)Z

    move-result v20

    new-instance v2, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;

    invoke-direct {v2, v0, v3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardHideClipboardToggleAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    move-object/from16 v21, v2

    invoke-direct/range {v16 .. v21}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$ToggleAction;)V

    move-object/from16 v2, v16

    invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"""
            content_norm = content_norm[:insert_pos] + "\n" + filter_rows_only + content_norm[insert_pos:]
            with open(settings_activity_path, "w", encoding="utf-8") as f:
                f.write(content_norm)
            print("Added Hide Toolbar Buttons toggle rows to GboardPatchesSettingsActivity")

if "GboardToolbarPositionToggleAction" not in content_norm:
    idx_autofill = content_norm.find("GboardAutofillToggleAction")
    if idx_autofill != -1:
        target_add = "invoke-interface {v5, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z"
        idx_add = content_norm.find(target_add, idx_autofill)
        if idx_add != -1:
            insert_pos = idx_add + len(target_add)
            content_norm = content_norm[:insert_pos] + "\n" + position_and_order_rows_smali + content_norm[insert_pos:]
            with open(settings_activity_path, "w", encoding="utf-8") as f:
                f.write(content_norm)
            print("Added ToolbarPosition and ToolbarOrder rows to GboardPatchesSettingsActivity")
else:
    print("ToolbarPosition rows already present")

# 3. Clean hook for SoftKeyboardView.smali
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
    print("SoftKeyboardView dispatchTouchEvent hook already in clean state")
else:
    print("Warning: SoftKeyboardView dispatchTouchEvent hook not matched")

# 3.1 Hook onAttachedToWindow
target_attach = "invoke-super {p0}, Lcom/google/android/libraries/inputmethod/widgets/AdditionalPaddingFrameLayout;->onAttachedToWindow()V"
replacement_attach = """invoke-super {p0}, Lcom/google/android/libraries/inputmethod/widgets/AdditionalPaddingFrameLayout;->onAttachedToWindow()V

    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadButton;->onSoftKeyboardViewAttached(Landroid/view/View;)V"""

if "GboardProofreadButton;->onSoftKeyboardViewAttached" not in skv_norm and target_attach in skv_norm:
    skv_norm = skv_norm.replace(target_attach, replacement_attach, 1)
    print("Hooked onAttachedToWindow in SoftKeyboardView")

# 3.2 Hook onLayout
target_layout = "invoke-super/range {p0 .. p5}, Lcom/google/android/libraries/inputmethod/widgets/AdditionalPaddingFrameLayout;->onLayout(ZIIII)V"
replacement_layout = """invoke-super/range {p0 .. p5}, Lcom/google/android/libraries/inputmethod/widgets/AdditionalPaddingFrameLayout;->onLayout(ZIIII)V

    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadButton;->onSoftKeyboardViewAttached(Landroid/view/View;)V"""

if target_layout in skv_norm and replacement_layout not in skv_norm:
    skv_norm = skv_norm.replace(target_layout, replacement_layout, 1)
    print("Hooked onLayout in SoftKeyboardView")

with open(skv_path, "w", encoding="utf-8") as f:
    f.write(skv_norm)

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


# 5. Hook hom.smali to intercept native Jarvis button clicks
hom_path = "_decompiled/smali_classes2/hom.smali"
if os.path.exists(hom_path):
    with open(hom_path, "r", encoding="utf-8") as f:
        hom_content = f.read()

    hom_norm = hom_content.replace("\r\n", "\n")

    target_hom = """.method public final s()V
    .locals 2

    .line 1
    iget-object v0, p0, Lhom;->f:Lpvl;

    .line 2
    .line 3
    sget-object v1, Lhph;->a:Lpvl;

    .line 4
    .line 5
    if-ne v0, v1, :cond_0"""

    replacement_hom = """.method public final s()V
    .locals 2

    .line 1
    iget-object v0, p0, Lhom;->f:Lpvl;

    .line 2
    .line 3
    sget-object v1, Lhph;->a:Lpvl;

    .line 4
    .line 5
    if-ne v0, v1, :cond_0

    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onJarvisClicked(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_pixelboard_fallback

    return-void

    :cond_pixelboard_fallback"""

    if "GboardProofreadService;->onJarvisClicked" not in hom_norm and target_hom in hom_norm:
        hom_norm = hom_norm.replace(target_hom, replacement_hom, 1)
        with open(hom_path, "w", encoding="utf-8") as f:
            f.write(hom_norm)
        print("Hooked Jarvis click in hom.smali")
    else:
        print("Jarvis hook in hom.smali already present or not matched")

# 6. Create GboardSetYandexBackendAction.smali
yandex_action_smali = """\
.class public final Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;
.super Ljava/lang/Object;
.source "GboardSetYandexBackendAction.java"

# interfaces
.implements Ljava/lang/Runnable;


# instance fields
.field public final activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

.field public final preferences:Landroid/content/SharedPreferences;


# direct methods
.method public constructor <init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .registers 3

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iput-object p2, p0, Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;->preferences:Landroid/content/SharedPreferences;

    return-void
.end method


# virtual methods
.method public final run()V
    .registers 3

    iget-object v0, p0, Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;->activity:Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;

    iget-object v1, p0, Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;->preferences:Landroid/content/SharedPreferences;

    invoke-static {v0, v1}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;->setBackendYandex(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    return-void
.end method
"""

os.makedirs("_decompiled/smali/com/akshaykadam/pixelboard/extension/settings", exist_ok=True)
with open("_decompiled/smali/com/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction.smali", "w", encoding="utf-8") as f:
    f.write(yandex_action_smali)
print("Created GboardSetYandexBackendAction.smali")

# 7. Add setBackendYandex method and Yandex RadioRow to GboardPatchesSettingsActivity.smali
with open(settings_activity_path, "r", encoding="utf-8") as f:
    act_content = f.read()

act_norm = act_content.replace("\r\n", "\n")

# 7.1 Add setBackendYandex helper method if not present
set_backend_yandex_smali = """
.method public static setBackendYandex(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V
    .locals 1

    if-eqz p1, :cond_0

    const-string v0, "YANDEX"

    invoke-static {p1, v0}, Lcom/akshaykadam/pixelboard/extension/writingtools/GboardAiWritingToolsSettings;->writeBackendType(Landroid/content/SharedPreferences;Ljava/lang/String;)Z

    if-eqz p0, :cond_0

    invoke-direct {p0}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;->renderCurrentScreenSafely()V

    :cond_0
    return-void
.end method
"""

if ".method public static setBackendYandex" not in act_norm:
    act_norm += set_backend_yandex_smali
    print("Added setBackendYandex to GboardPatchesSettingsActivity")

# 7.2 Inject Yandex Speller RadioRow
backend_list_anchor = """    :goto_8
    new-instance v5, Ljava/util/ArrayList;

    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V"""

yandex_radiorow_smali = """    :goto_8
    new-instance v5, Ljava/util/ArrayList;

    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    # Yandex Speller RadioRow
    new-instance v7, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$RadioRow;

    const-string v3, "YANDEX"

    invoke-virtual {v3, v4}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v11

    new-instance v12, Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;

    invoke-direct {v12, v0, v1}, Lcom/akshaykadam/pixelboard/extension/settings/GboardSetYandexBackendAction;-><init>(Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;Landroid/content/SharedPreferences;)V

    invoke-direct {v0, v3}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsActivity;->createBackendIcon(Ljava/lang/String;)Landroid/graphics/drawable/Drawable;

    move-result-object v13

    const-string v8, "Yandex Speller (\\u042f\\u043d\\u0434\\u0435\\u043a\\u0441.\\u0421\\u043f\\u0435\\u043b\\u043b\\u0435\\u0440)"

    const-string v9, "\\u0418\\u0441\\u043f\\u0440\\u0430\\u0432\\u043b\\u0435\\u043d\\u0438\\u0435 \\u043e\\u0440\\u0444\\u043e\\u0433\\u0440\\u0430\\u0444\\u0438\\u0438 \\u0438 \\u043f\\u0443\\u043d\\u043a\\u0442\\u0443\\u0430\\u0446\\u0438\\u0438 \\u043f\\u043e \\u043d\\u043e\\u0440\\u043c\\u0430\\u043c \\u0440\\u0443\\u0441\\u0441\\u043a\\u043e\\u0433\\u043e \\u044f\\u0437\\u044b\\u043a\\u0430 \\u0441 Git Diff \\u043a\\u0430\\u0440\\u0442\\u043e\\u0447\\u043a\\u043e\\u0439."

    invoke-direct/range {v7 .. v13}, Lcom/akshaykadam/pixelboard/extension/settings/GboardPatchesSettingsContract$RadioRow;-><init>(Ljava/lang/CharSequence;Ljava/lang/String;ZZLjava/lang/Runnable;Landroid/graphics/drawable/Drawable;)V

    invoke-interface {v5, v7}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    const-string v3, "GBOARD_SERVER" """

if "GboardSetYandexBackendAction" not in act_norm and backend_list_anchor in act_norm:
    act_norm = act_norm.replace(backend_list_anchor, yandex_radiorow_smali, 1)
    print("Injected Yandex Speller RadioRow into GboardPatchesSettingsActivity")
else:
    print("Yandex Speller RadioRow already present or anchor not found")

with open(settings_activity_path, "w", encoding="utf-8") as f:
    f.write(act_norm)

# 9. Hook hjk.smali (toolbar Jarvis click handler)
hjk_path = "_decompiled/smali_classes3/hjk.smali"
if os.path.exists(hjk_path):
    with open(hjk_path, "r", encoding="utf-8") as f:
        hjk_content = f.read()

    hjk_norm = hjk_content.replace("\r\n", "\n")

    target_hjk = """    sget-object v4, Lhph;->a:Lpvl;

    .line 49
    .line 50
    invoke-virtual {v2, v4}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 51
    .line 52
    .line 53
    move-result v2

    .line 54
    if-eqz v2, :cond_4

    .line 55
    .line 56
    iget-object p0, p0, Lhjk;->e:Lhjv;"""

    replacement_hjk = """    sget-object v4, Lhph;->a:Lpvl;

    .line 49
    .line 50
    invoke-virtual {v2, v4}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    .line 51
    .line 52
    .line 53
    move-result v2

    .line 54
    if-eqz v2, :cond_4

    invoke-static {p0}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onJarvisClicked(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_pixelboard_hjk_normal

    return v1

    :cond_pixelboard_hjk_normal
    .line 55
    .line 56
    iget-object p0, p0, Lhjk;->e:Lhjv;"""

    if "GboardProofreadService;->onJarvisClicked" not in hjk_norm and target_hjk in hjk_norm:
        hjk_norm = hjk_norm.replace(target_hjk, replacement_hjk, 1)
        with open(hjk_path, "w", encoding="utf-8") as f:
            f.write(hjk_norm)
        print("Hooked toolbar Jarvis click in hjk.smali")
    else:
        print("hjk.smali hook already present or not matched")

# 8. Hook hrz.smali (JarvisPromptExtension.t) to intercept Google Jarvis UI when opened from the 4-squares menu
hrz_path = "_decompiled/smali_classes2/hrz.smali"
if os.path.exists(hrz_path):
    with open(hrz_path, "r", encoding="utf-8") as f:
        hrz_content = f.read()

    hrz_norm = hrz_content.replace("\r\n", "\n")

    target_hrz_t = """.method protected final t(Ljava/util/Map;Lobf;)V
    .locals 21

    .line 1
    move-object/from16 v0, p0"""

    replacement_hrz_t = """.method protected final t(Ljava/util/Map;Lobf;)V
    .locals 21

    move-object/from16 v0, p0

    invoke-static {v0}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onJarvisClicked(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_pixelboard_t_normal

    return-void

    :cond_pixelboard_t_normal
    .line 1
    move-object/from16 v0, p0"""

    if "GboardProofreadService;->onJarvisClicked" not in hrz_norm and target_hrz_t in hrz_norm:
        hrz_norm = hrz_norm.replace(target_hrz_t, replacement_hrz_t, 1)
        with open(hrz_path, "w", encoding="utf-8") as f:
            f.write(hrz_norm)
        print("Hooked JarvisPromptExtension.t in hrz.smali")
    else:
        print("hrz.smali t hook already present or not matched")

# 9. Hook hnk.smali (OnAccessPointClickListener.a) - Direct click listener for the Jarvis toolbar button [✎✨]
hnk_path = "_decompiled/smali_classes3/hnk.smali"
if os.path.exists(hnk_path):
    with open(hnk_path, "r", encoding="utf-8") as f:
        hnk_content = f.read()
    hnk_norm = hnk_content.replace("\r\n", "\n")

    target_hnk = """.method public final a(Lmnj;Landroid/view/View;)V
    .locals 17

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    move-object/from16 v1, p1

    .line 4
    .line 5
    move-object/from16 v2, p2

    .line 6
    .line 7
    iget v3, v0, Lhnk;->b:I"""

    replacement_hnk = """.method public final a(Lmnj;Landroid/view/View;)V
    .locals 17

    .line 1
    move-object/from16 v0, p0

    .line 2
    .line 3
    move-object/from16 v1, p1

    .line 4
    .line 5
    move-object/from16 v2, p2

    invoke-static {v2}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onJarvisClicked(Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_pixelboard_hnk_normal

    return-void

    :cond_pixelboard_hnk_normal
    .line 6
    .line 7
    iget v3, v0, Lhnk;->b:I"""

    if "GboardProofreadService;->onJarvisClicked" not in hnk_norm and target_hnk in hnk_norm:
        hnk_norm = hnk_norm.replace(target_hnk, replacement_hnk, 1)
        with open(hnk_path, "w", encoding="utf-8") as f:
            f.write(hnk_norm)
        print("Hooked Jarvis access point click in hnk.smali")
    else:
        print("hnk.smali hook already present or not matched")

# 10. Hook hno.smali:q(Lmnj;Landroid/view/View;)V - Jarvis access point action dispatcher
hno_path = "_decompiled/smali_classes3/hno.smali"
if os.path.exists(hno_path):
    with open(hno_path, "r", encoding="utf-8") as f:
        hno_content = f.read()
    hno_norm = hno_content.replace("\r\n", "\n")

    target_hno = """.method public final q(Lmnj;Landroid/view/View;)V
    .locals 6

    .line 1
    iget-boolean v0, p0, Lhno;->l:Z"""

    replacement_hno = """.method public final q(Lmnj;Landroid/view/View;)V
    .locals 6

    invoke-static {p2}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onJarvisClicked(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_pixelboard_hno_normal

    return-void

    :cond_pixelboard_hno_normal
    .line 1
    iget-boolean v0, p0, Lhno;->l:Z"""

    if "GboardProofreadService;->onJarvisClicked" not in hno_norm and target_hno in hno_norm:
        hno_norm = hno_norm.replace(target_hno, replacement_hno, 1)
        with open(hno_path, "w", encoding="utf-8") as f:
            f.write(hno_norm)
        print("Hooked Jarvis action dispatcher in hno.smali")
    else:
        print("hno.smali hook already present or not matched")


# 11. Hook hip.smali:y(Lqrw;)Lxin; - Intercept native Writing Tools proofread call
# 12. Hook hip.smali:t(Ljava/lang/CharSequence;Ljava/lang/CharSequence;I)Landroid/text/Spannable; - Native card Git Diff formatting
# 13. Hook hip.smali:z(Lomp;Lqrw;Lojz;)Lxin; - Intercept native Writing Tools rewrite/style call
hip_path = "_decompiled/smali_classes2/hip.smali"
if os.path.exists(hip_path):
    with open(hip_path, "r", encoding="utf-8") as f:
        hip_content = f.read()
    hip_norm = hip_content.replace("\r\n", "\n")

    target_hip_y = """.method public final y(Lqrw;)Lxin;
    .locals 14

    .line 1
    iget-boolean v0, p0, Lhip;->u:Z"""

    replacement_hip_y = """.method public final y(Lqrw;)Lxin;
    .locals 14

    invoke-static {p0, p1}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onGetGenAiProofreadResponse(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    if-eqz v0, :cond_pixelboard_hip_y_normal

    check-cast v0, Lxin;

    return-object v0

    :cond_pixelboard_hip_y_normal
    .line 1
    iget-boolean v0, p0, Lhip;->u:Z"""

    if "GboardProofreadService;->onGetGenAiProofreadResponse" not in hip_norm and target_hip_y in hip_norm:
        hip_norm = hip_norm.replace(target_hip_y, replacement_hip_y, 1)
        print("Hooked proofread engine in hip.smali:y")
    else:
        print("hip.smali:y hook already present or not matched")

    target_hip_t = """.method public final t(Ljava/lang/CharSequence;Ljava/lang/CharSequence;I)Landroid/text/Spannable;
    .locals 6

    .line 1
    iget-object v0, p0, Lhip;->w:Lgyf;"""

    replacement_hip_t = """.method public final t(Ljava/lang/CharSequence;Ljava/lang/CharSequence;I)Landroid/text/Spannable;
    .locals 6

    invoke-static {p1, p2, p3}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->formatProofreadDiffSpannable(Ljava/lang/CharSequence;Ljava/lang/CharSequence;I)Landroid/text/Spannable;

    move-result-object v0

    if-eqz v0, :cond_pixelboard_hip_t_normal

    return-object v0

    :cond_pixelboard_hip_t_normal
    .line 1
    iget-object v0, p0, Lhip;->w:Lgyf;"""

    if "GboardProofreadService;->formatProofreadDiffSpannable" not in hip_norm and target_hip_t in hip_norm:
        hip_norm = hip_norm.replace(target_hip_t, replacement_hip_t, 1)
        print("Hooked Git Diff formatting in hip.smali:t")
    else:
        print("hip.smali:t hook already present or not matched")

    target_hip_z = """.method public final z(Lomp;Lqrw;Lojz;)Lxin;
    .locals 18

    .line 1
    move-object/from16 v1, p0

    .line 2
    .line 3
    move-object/from16 v2, p1

    .line 4
    .line 5
    move-object/from16 v0, p2

    .line 6
    .line 7
    const/4 v3, 0x1"""

    replacement_hip_z = """.method public final z(Lomp;Lqrw;Lojz;)Lxin;
    .locals 18

    invoke-static/range {p0 .. p2}, Lcom/akshaykadam/pixelboard/extension/proofread/GboardProofreadService;->onGetGenAiRewriteResponse(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    if-eqz v0, :cond_pixelboard_hip_z_normal

    check-cast v0, Lxin;

    return-object v0

    :cond_pixelboard_hip_z_normal
    .line 1
    move-object/from16 v1, p0

    .line 2
    .line 3
    move-object/from16 v2, p1

    .line 4
    .line 5
    move-object/from16 v0, p2

    .line 6
    .line 7
    const/4 v3, 0x1"""

    if "GboardProofreadService;->onGetGenAiRewriteResponse" not in hip_norm and target_hip_z in hip_norm:
        hip_norm = hip_norm.replace(target_hip_z, replacement_hip_z, 1)
        print("Hooked rewrite engine in hip.smali:z")
    else:
        print("hip.smali:z hook already present or not matched")

    with open(hip_path, "w", encoding="utf-8") as f:
        f.write(hip_norm)


# 14. Patch gjl.smali:ab()Z and ac(Z)Z to bypass Google Cloud GenAi eligibility checks
gjl_path = "_decompiled/smali_classes3/gjl.smali"
if os.path.exists(gjl_path):
    with open(gjl_path, "r", encoding="utf-8") as f:
        gjl_content = f.read()
    gjl_norm = gjl_content.replace("\r\n", "\n")

    target_gjl_ab = """.method public static ab()Z
    .locals 1

    .line 1
    invoke-static {}, Lojm;->c()Lojm;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    invoke-virtual {v0}, Lojm;->a()Loiz;

    .line 6
    .line 7
    .line 8
    move-result-object v0

    .line 9
    invoke-virtual {v0}, Loiz;->b()Z

    .line 10
    .line 11
    .line 12
    move-result v0

    .line 13
    return v0
.end method"""

    replacement_gjl_ab = """.method public static ab()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method"""

    if target_gjl_ab in gjl_norm:
        gjl_norm = gjl_norm.replace(target_gjl_ab, replacement_gjl_ab, 1)
        print("Patched gjl.smali:ab() to return true")
    else:
        print("gjl.smali:ab() already patched or not matched")

    target_gjl_ac = """.method public static ac(Z)Z
    .locals 1

    .line 1
    invoke-static {}, Lojm;->c()Lojm;

    .line 2
    .line 3
    .line 4
    move-result-object v0

    .line 5
    invoke-virtual {v0}, Lojm;->j()Z

    .line 6
    .line 7
    .line 8
    move-result v0

    .line 9
    if-eqz v0, :cond_1

    .line 10
    .line 11
    if-eqz p0, :cond_0

    .line 12
    .line 13
    goto :goto_0

    .line 14
    :cond_0
    const/4 p0, 0x0

    .line 15
    return p0

    .line 16
    :cond_1
    :goto_0
    const/4 p0, 0x1

    .line 17
    return p0
.end method"""

    replacement_gjl_ac = """.method public static ac(Z)Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method"""

    if target_gjl_ac in gjl_norm:
        gjl_norm = gjl_norm.replace(target_gjl_ac, replacement_gjl_ac, 1)
        print("Patched gjl.smali:ac(Z) to return true")
    else:
        print("gjl.smali:ac(Z) already patched or not matched")

    with open(gjl_path, "w", encoding="utf-8") as f:
        f.write(gjl_norm)


# 15. Patch hny.smali:c(Landroid/content/Context;)V to neuter "Writing tools unavailable" popup/toast
hny_path = "_decompiled/smali_classes3/hny.smali"
if os.path.exists(hny_path):
    with open(hny_path, "r", encoding="utf-8") as f:
        hny_content = f.read()
    hny_norm = hny_content.replace("\r\n", "\n")

    c_sig = ".method public static c(Landroid/content/Context;)V"
    if c_sig in hny_norm:
        c_idx = hny_norm.find(c_sig)
        end_idx = hny_norm.find(".end method", c_idx) + len(".end method")
        old_c_method = hny_norm[c_idx:end_idx]
        new_c_method = """.method public static c(Landroid/content/Context;)V
    .locals 0

    return-void
.end method"""
        if old_c_method != new_c_method:
            hny_norm = hny_norm[:c_idx] + new_c_method + hny_norm[end_idx:]
            print("Patched hny.smali:c() to return-void")
            with open(hny_path, "w", encoding="utf-8") as f:
                f.write(hny_norm)
        else:
            print("hny.smali:c() already patched")


# 16. Patch ijd.smali:L(Ljava/lang/Runnable;)Z to bypass aicore_not_working_toast
ijd_path = "_decompiled/smali_classes3/ijd.smali"
if os.path.exists(ijd_path):
    with open(ijd_path, "r", encoding="utf-8") as f:
        ijd_content = f.read()
    ijd_norm = ijd_content.replace("\r\n", "\n")

    l_sig = ".method private final L(Ljava/lang/Runnable;)Z"
    if l_sig in ijd_norm:
        l_idx = ijd_norm.find(l_sig)
        end_idx = ijd_norm.find(".end method", l_idx) + len(".end method")
        old_l_method = ijd_norm[l_idx:end_idx]
        new_l_method = """.method private final L(Ljava/lang/Runnable;)Z
    .locals 1

    const/4 v0, 0x0

    return v0
.end method"""
        if old_l_method != new_l_method:
            ijd_norm = ijd_norm[:l_idx] + new_l_method + ijd_norm[end_idx:]
            print("Patched ijd.smali:L() to return false (0)")
            with open(ijd_path, "w", encoding="utf-8") as f:
                f.write(ijd_norm)
        else:
            print("ijd.smali:L() already patched")


# 17. Patch qeq.smali:e(Lqem;)Z to unconditionally enable qrr.g (Writing Tools Phenotype flag)
qeq_path = "_decompiled/smali_classes2/qeq.smali"
if os.path.exists(qeq_path):
    with open(qeq_path, "r", encoding="utf-8") as f:
        qeq_content = f.read()
    qeq_norm = qeq_content.replace("\r\n", "\n")

    target_qeq_e = """.method public static e(Lqem;)Z
    .locals 2

    .line 1
    invoke-static {}, Lqew;->b()Lqew;"""

    replacement_qeq_e = """.method public static e(Lqem;)Z
    .locals 2

    sget-object v0, Lqrr;->g:Lqem;

    if-ne p0, v0, :cond_pixelboard_qeq_e_normal

    const/4 v0, 0x1

    return v0

    :cond_pixelboard_qeq_e_normal
    .line 1
    invoke-static {}, Lqew;->b()Lqew;"""

    if "cond_pixelboard_qeq_e_normal" not in qeq_norm and target_qeq_e in qeq_norm:
        qeq_norm = qeq_norm.replace(target_qeq_e, replacement_qeq_e, 1)
        print("Patched qeq.smali:e() for qrr.g flag")
        with open(qeq_path, "w", encoding="utf-8") as f:
            f.write(qeq_norm)
    else:
        print("qeq.smali:e() already patched or not matched")
