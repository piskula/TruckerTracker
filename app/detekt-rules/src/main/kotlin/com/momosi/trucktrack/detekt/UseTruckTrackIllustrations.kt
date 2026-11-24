package com.momosi.trucktrack.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class UseTruckTrackIllustrations(config: Config) :
    Rule(
        config,
        "Drawable illustrations must be exposed through TruckTrackIllustrations.",
    ) {
    private var resourceNames = emptySet<String>()
    private var illustrationNames = emptySet<String>()

    override fun visitKtFile(file: KtFile) {
        if (
            file.name == "TruckTrackIllustrations.kt" &&
            file.packageFqName.asString() == "com.momosi.trucktrack.core.uilibrary.illustrations"
        ) {
            return
        }
        resourceNames = file.importDirectives.mapNotNull { directive ->
            val name = directive.importedFqName?.asString()
            when {
                name?.endsWith(".resources.Res") == true && !directive.isAllUnder ->
                    directive.aliasName ?: "Res"

                name?.endsWith(".resources") == true && directive.isAllUnder -> "Res"

                else -> null
            }
        }.toSet()
        illustrationNames = file.importDirectives.mapNotNull { directive ->
            directive.importedFqName?.shortName()?.asString()
                ?.takeIf { it.endsWith("_illustration") }
                ?.let { directive.aliasName ?: it }
        }.toSet()
        super.visitKtFile(file)
        resourceNames = emptySet()
        illustrationNames = emptySet()
    }

    override fun visitImportDirective(importDirective: KtImportDirective) {
        val name = importDirective.importedFqName?.asString() ?: return
        if (
            name.endsWith(".resources.Res.drawable") ||
            name.contains(".resources.Res.drawable.") ||
            (name.endsWith(".resources.Res") && importDirective.isAllUnder)
        ) {
            report(Finding(Entity.from(importDirective), MESSAGE))
        }
    }

    override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
        super.visitDotQualifiedExpression(expression)
        val selector = expression.selectorExpression as? KtNameReferenceExpression ?: return
        if (selector.getReferencedName() != "drawable") return
        val receiver = expression.receiverExpression
        val isImportedResource = receiver is KtNameReferenceExpression &&
            receiver.getReferencedName() in resourceNames
        val isQualifiedResource = receiver is KtDotQualifiedExpression &&
            receiver.text.endsWith(".resources.Res")
        if (isImportedResource || isQualifiedResource) {
            val access = expression.parent as? KtDotQualifiedExpression
            val resource = (access?.selectorExpression as? KtNameReferenceExpression)?.getReferencedName()
            if (
                access?.receiverExpression == expression &&
                resource != null &&
                !resource.endsWith("_illustration") &&
                resource !in illustrationNames
            ) {
                return
            }
            report(Finding(Entity.from(expression), MESSAGE))
        }
    }

    private companion object {
        const val MESSAGE = "Access illustrations through TruckTrackIllustrations, not Res.drawable."
    }
}
