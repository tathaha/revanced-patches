package app.revanced.patches.youtube.layout.hidecrowdfundingbox.bytecode.patch

import app.revanced.patcher.BytecodeContext
import app.revanced.patcher.annotation.Description
import app.revanced.patcher.annotation.Name
import app.revanced.patcher.annotation.Version
import app.revanced.patcher.extensions.addInstruction
import app.revanced.patcher.extensions.instruction
import app.revanced.patcher.patch.BytecodePatch
import app.revanced.patcher.patch.PatchResult
import app.revanced.patcher.patch.annotations.DependsOn
import app.revanced.patcher.patch.annotations.Patch
import app.revanced.patches.youtube.layout.hidecrowdfundingbox.annotations.CrowdfundingBoxCompatibility
import app.revanced.patches.youtube.layout.hidecrowdfundingbox.bytecode.fingerprints.CrowdfundingBoxFingerprint
import app.revanced.patches.youtube.layout.hidecrowdfundingbox.resource.patch.CrowdfundingBoxResourcePatch
import app.revanced.patches.youtube.misc.integrations.patch.IntegrationsPatch
import org.jf.dexlib2.iface.instruction.OneRegisterInstruction

@Patch
@DependsOn([IntegrationsPatch::class, CrowdfundingBoxResourcePatch::class])
@Name("hide-crowdfunding-box")
@Description("Hides the crowdfunding box between the player and video description.")
@CrowdfundingBoxCompatibility
@Version("0.0.1")
class CrowdfundingBoxPatch : BytecodePatch(
    listOf(
        CrowdfundingBoxFingerprint,
    )
) {
    override fun execute(context: BytecodeContext): PatchResult {
        val crowdfundingBoxResult = CrowdfundingBoxFingerprint.result!!
        val crowdfundingBoxMethod = crowdfundingBoxResult.mutableMethod

        val moveResultObjectIndex =
            crowdfundingBoxResult.scanResult.patternScanResult!!.endIndex - 2

        crowdfundingBoxMethod.addInstruction(
            moveResultObjectIndex + 1, """
            invoke-static {v${(crowdfundingBoxMethod.instruction(moveResultObjectIndex) as OneRegisterInstruction).registerA}}, Lapp/revanced/integrations/patches/HideCrowdfundingBoxPatch;->hideCrowdfundingBox(Landroid/view/View;)V
        """
        )

        return PatchResult.Success
    }
}
