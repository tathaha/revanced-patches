package app.revanced.patches.youtube.misc.video.quality.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.addInstructions
import app.revanced.patcher.fingerprint.method.impl.MethodFingerprint.Companion.resolve
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import app.revanced.patches.youtube.misc.video.quality.annotations.RememberVideoQualityCompatibility
import app.revanced.patches.youtube.misc.video.quality.fingerprints.VideoQualityReferenceFingerprint
import app.revanced.patches.youtube.misc.video.quality.fingerprints.VideoQualitySetterFingerprint
import app.revanced.patches.youtube.misc.video.quality.fingerprints.VideoUserQualityChangeFingerprint
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.video.videoid.patch.VideoIdPatch
import org.jf.dexlib2.iface.instruction.ReferenceInstruction
import org.jf.dexlib2.iface.reference.FieldReference

@Patch
@DependsOn([IntegrationsPatch::class, VideoIdPatch::class, SettingsPatch::class])
@Name("remember-video-quality")
@Description("Adds the ability to remember the video quality you chose in the video quality flyout.")
@RememberVideoQualityCompatibility
@Version("0.0.1")
class RememberVideoQualityPatch : BytecodePatch(
    listOf(
        VideoQualitySetterFingerprint
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            SwitchPreference(
                "revanced_remember_video_quality_last_selected",
                StringResource("revanced_remember_video_quality_last_selected_title", "Remember video quality changes"),
                true,
                StringResource(
                    "revanced_remember_video_quality_last_selected_summary_on",
                    "Quality changes apply to all videos"
                ),
                StringResource(
                    "revanced_remember_video_quality_last_selected_summary_off",
                    "Quality changes only apply to the current video and are reverted back to the last remembered quality for future playbacks"
                )
            )
        )

        val setterMethod = VideoQualitySetterFingerprint.result!!

        VideoUserQualityChangeFingerprint.resolve(context, setterMethod.classDef)
        val userQualityMethod = VideoUserQualityChangeFingerprint.result!!

        VideoQualityReferenceFingerprint.resolve(context, setterMethod.classDef)
        val qualityFieldReference =
            VideoQualityReferenceFingerprint.result!!.method.let { method ->
                (method.implementation!!.instructions.elementAt(0) as ReferenceInstruction).reference as FieldReference
            }

        VideoIdPatch.injectCall("Lapp/revanced/integrations/patches/playback/quality/RememberVideoQualityPatch;->newVideoStarted(Ljava/lang/String;)V")

        val qIndexMethodName =
            context.classes.single { it.type == qualityFieldReference.type }.methods.single { it.parameterTypes.first() == "I" }.name

        setterMethod.mutableMethod.addInstructions(
            0,
            """
                iget-object v0, p0, ${setterMethod.classDef.type}->${qualityFieldReference.name}:${qualityFieldReference.type}
                const-string v1, "$qIndexMethodName"
		        invoke-static {p1, p2, v0, v1}, Lapp/revanced/integrations/patches/playback/quality/RememberVideoQualityPatch;->setVideoQuality([Ljava/lang/Object;ILjava/lang/Object;Ljava/lang/String;)I
   		        move-result p2
            """,
        )

        userQualityMethod.mutableMethod.addInstruction(
            0,
            "invoke-static {p3}, Lapp/revanced/integrations/patches/playback/quality/RememberVideoQualityPatch;->userChangedQuality(I)V"
        )

        return PatchResult.Success
    }
}
