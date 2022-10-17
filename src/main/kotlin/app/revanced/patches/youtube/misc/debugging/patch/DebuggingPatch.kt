package app.revanced.patches.youtube.misc.debugging.patch

import app.revanced.patcher.ResourceContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.patch.*
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.shared.settings.preference.impl.StringResource
import app.revanced.patches.shared.settings.preference.impl.SwitchPreference
import app.revanced.patches.youtube.misc.debugging.annotations.DebuggingCompatibility
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import app.revanced.patches.youtube.misc.settings.bytecode.patch.SettingsPatch
import org.w3c.dom.Element

@Patch
@Name("debugging")
@DependsOn([IntegrationsPatch::class, SettingsPatch::class])
@Description("Adds debugging options.")
@DebuggingCompatibility
@Version("0.0.1")
class DebuggingPatch : ResourcePatch {
    override fun execute(context: ResourceContext): PatchResult {
        SettingsPatch.PreferenceScreen.MISC.addPreferences(
            app.revanced.patches.shared.settings.preference.impl.PreferenceScreen(
                "revanced_debug",
                StringResource("revanced_debug_title", "Debugging"),
                listOf(
                    SwitchPreference(
                        "revanced_debug_enabled",
                        StringResource("revanced_debug_enabled_title", "Enable debug logs"),
                        false,
                        StringResource("revanced_debug_summary_on", "Debug logs are enabled"),
                        StringResource("revanced_debug_summary_off", "Debug logs are disabled")
                    ),
                    SwitchPreference(
                        "revanced_debug_stacktrace_enabled",
                        StringResource(
                            "revanced_debug_stacktrace_enabled_title",
                            "Print stack traces"
                        ),
                        false,
                        StringResource("revanced_debug_stacktrace_summary_on", "Enabled printing stack traces"),
                        StringResource("revanced_debug_stacktrace_summary_off", "Disabled printing stack traces")
                    ),
                ),
                StringResource("revanced_debug_summary", "Enable or disable debugging options")
            )
        )

        if (debuggable == true) {
            context.openEditor("AndroidManifest.xml").use { dom ->
                val applicationNode = dom
                    .file
                    .getElementsByTagName("application")
                    .item(0) as Element

                // set application as debuggable
                applicationNode.setAttribute("android:debuggable", "true")
            }
        }

        return PatchResult.Success
    }

    companion object : OptionsContainer() {
        var debuggable: Boolean? by option(
            PatchOption.BooleanOption(
                key = "debuggable",
                default = false,
                title = "App debugging",
                description = "Whether to make the app debuggable on Android.",
            )
        )
    }
}
