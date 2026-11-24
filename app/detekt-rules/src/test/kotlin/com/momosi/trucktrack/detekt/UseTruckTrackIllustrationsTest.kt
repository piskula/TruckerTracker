package com.momosi.trucktrack.detekt

import dev.detekt.api.Config
import dev.detekt.test.lint
import dev.detekt.test.utils.compileContentForTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UseTruckTrackIllustrationsTest {
    @Test
    fun `reports direct drawable access and captured drawable collections`() {
        val findings = UseTruckTrackIllustrations(Config.empty).lint(
            """
            import com.momosi.trucktrack.core.uilibrary.resources.Res
            import com.momosi.trucktrack.core.uilibrary.resources.error_no_connection_illustration

            val illustration = Res.drawable.error_no_connection_illustration
            val drawables = Res.drawable
            """.trimIndent(),
        )

        assertEquals(2, findings.size)
    }

    @Test
    fun `reports aliases fully qualified access and wildcard resource imports`() {
        val snippets = listOf(
            """
            import com.momosi.trucktrack.core.uilibrary.resources.Res as Artwork
            val illustration = Artwork.drawable.error_generic_illustration
            """,
            """
            import com.momosi.trucktrack.core.uilibrary.resources.Res.drawable as Artwork
            val illustration = Artwork.error_generic_illustration
            """,
            """
            val illustration = com.momosi.trucktrack.core.uilibrary.resources.Res.drawable.error_generic_illustration
            """,
            """
            import com.momosi.trucktrack.core.uilibrary.resources.*
            val illustration = Res.drawable.error_generic_illustration
            """,
            """
            import com.momosi.trucktrack.core.uilibrary.resources.Res.*
            val illustration = drawable.error_generic_illustration
            """,
            """
            import com.momosi.trucktrack.core.uilibrary.resources.Res
            import com.momosi.trucktrack.core.uilibrary.resources.error_generic_illustration as Artwork
            val illustration = Res.drawable.Artwork
            """,
        )

        snippets.forEach { code ->
            assertEquals(1, UseTruckTrackIllustrations(Config.empty).lint(code.trimIndent()).size, code)
        }
    }

    @Test
    fun `allows catalogue references strings and unrelated drawable properties`() {
        val findings = UseTruckTrackIllustrations(Config.empty).lint(
            """
            import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
            import com.momosi.trucktrack.core.uilibrary.illustrations.TruckTrackIllustrations
            import com.momosi.trucktrack.core.uilibrary.resources.Res

            val illustration = TruckTrackIllustrations.NoConnection
            val icon = TruckTrackIcons.Truck
            val text = Res.string.retry
            val photo = Res.drawable.sign_in_hero
            val unrelated = vehicle.drawable
            """.trimIndent(),
        )

        assertTrue(findings.isEmpty())
    }

    @Test
    fun `only allows direct resource access in the canonical catalogue file`() {
        val code = """
            package com.momosi.trucktrack.core.uilibrary.illustrations

            import com.momosi.trucktrack.core.uilibrary.resources.Res
            import com.momosi.trucktrack.core.uilibrary.resources.error_no_connection_illustration

            data object TruckTrackIllustrations {
                val NoConnection = Res.drawable.error_no_connection_illustration
            }
        """.trimIndent()
        val rule = UseTruckTrackIllustrations(Config.empty)

        assertTrue(rule.lint(compileContentForTest(code, filename = "TruckTrackIllustrations.kt")).isEmpty())
        assertEquals(1, rule.lint(compileContentForTest(code, filename = "Other.kt")).size)
        assertEquals(
            1,
            rule.lint(
                compileContentForTest(
                    code.replace(
                        "package com.momosi.trucktrack.core.uilibrary.illustrations",
                        "package com.momosi.trucktrack.feature.issues",
                    ),
                    filename = "TruckTrackIllustrations.kt",
                ),
            ).size,
        )
    }
}
