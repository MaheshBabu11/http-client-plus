
/*
 * Copyright 2025 Mahesh Babu (MaheshBabu11)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2025 Mahesh Babu (MaheshBabu11)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2025 Mahesh Babu (MaheshBabu11)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.maheshbabu11.httpclientplus.ui

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.util.Base64
import javax.swing.*
import com.intellij.openapi.ui.ComboBox
import java.awt.Component

class AuthorizationSection {
    private val typeBox = ComboBox(
        arrayOf(
            "None",
            "Basic",
            "Bearer token",
            "Digest",
            "Custom header"
        )
    )

    private val basicUserField = JBTextField()
    private val basicPassField = JBTextField()

    private val bearerTokenField = JBTextField()

    private val digestUserField = JBTextField()
    private val digestPassField = JBTextField()

    private val customKeyField = JBTextField("Authorization")
    private val customValueField = JBTextField()

    private val cards = JPanel(CardLayout())

    val component: JComponent = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)

        val typeBoxPreferredHeight = 32
        val typeBoxPreferredWidth = 220 // or use typeBox.preferredSize.width for default

        typeBox.maximumSize = Dimension(typeBoxPreferredWidth, typeBoxPreferredHeight)
        typeBox.preferredSize = Dimension(typeBoxPreferredWidth, typeBoxPreferredHeight)
        typeBox.minimumSize = Dimension(typeBoxPreferredWidth, typeBoxPreferredHeight)

        val top = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            alignmentX = JComponent.LEFT_ALIGNMENT
            add(JLabel("Type:").apply { alignmentY = Component.CENTER_ALIGNMENT })
            add(Box.createHorizontalStrut(8))
            add(typeBox.apply { alignmentY = Component.CENTER_ALIGNMENT })
        }
        // Build cards
        cards.add(buildNonePanel(), "None")
        cards.add(buildBasicPanel(), "Basic")
        cards.add(buildBearerPanel(), "Bearer token")
        cards.add(buildDigestPanel(), "Digest")
        cards.add(buildCustomPanel(), "Custom header")


        /// Place top and cards into a single vertical container
        val content = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty()

            top.alignmentX = JComponent.LEFT_ALIGNMENT
            add(top)
            add(Box.createVerticalStrut(8)) // Small gap between selector and cards

            // Wrap cards so they stay at the top instead of stretching
            val cardWrapper = JPanel(BorderLayout()).apply {
                add(cards, BorderLayout.NORTH)
            }
            cardWrapper.alignmentX = JComponent.LEFT_ALIGNMENT
            add(cardWrapper)
        }
        add(content, BorderLayout.CENTER)

        typeBox.addActionListener {
            (cards.layout as CardLayout).show(cards, typeBox.selectedItem as String)
        }
        // Initialize card
        (cards.layout as CardLayout).show(cards, typeBox.selectedItem as String)
    }

    private fun buildNonePanel(): JComponent = JPanel()

    private fun buildBasicPanel(): JComponent = JPanel(GridBagLayout()).apply {
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0; anchor = GridBagConstraints.WEST; insets = JBUI.insets(0, 0, 8, 8)
        }
        add(JLabel("Username"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(basicUserField, gbc)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE
        add(JLabel("Password"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(basicPassField, gbc)
        val hint = JLabel("Tip: use {{var}} to reference variables").apply { border = JBUI.Borders.emptyTop(6) }
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST
        add(hint, gbc)
    }

    private fun buildBearerPanel(): JComponent = JPanel(GridBagLayout()).apply {
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0; anchor = GridBagConstraints.WEST; insets = JBUI.insets(0, 0, 8, 8)
        }
        add(JLabel("Token"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(bearerTokenField, gbc)
    }

    private fun buildDigestPanel(): JComponent = JPanel(GridBagLayout()).apply {
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0; anchor = GridBagConstraints.WEST; insets = JBUI.insets(0, 0, 8, 8)
        }
        add(JLabel("Username"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(digestUserField, gbc)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE
        add(JLabel("Password"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(digestPassField, gbc)
        val hint = JLabel("Tip: use {{var}} to reference variables").apply { border = JBUI.Borders.emptyTop(6) }
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.WEST
        add(hint, gbc)
    }

    private fun buildCustomPanel(): JComponent = JPanel(GridBagLayout()).apply {
        val gbc = GridBagConstraints().apply {
            gridx = 0; gridy = 0; anchor = GridBagConstraints.WEST; insets = JBUI.insets(0, 0, 8, 8)
        }
        add(JLabel("Key"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(customKeyField, gbc)
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE
        add(JLabel("Value"), gbc)
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0
        add(customValueField, gbc)
    }

    private fun isVarPlaceholder(text: String): Boolean {
        val t = text.trim()
        return t.startsWith("{{") && t.endsWith("}}") && t.length >= 4
    }

    fun getAuthorizationHeader(): Pair<String, String>? {
        return when (typeBox.selectedItem as String) {
            "None" -> null
            "Basic" -> {
                val user = basicUserField.text.orEmpty().trim()
                val pass = basicPassField.text.orEmpty().trim()
                if (user.isEmpty() && pass.isEmpty()) return null
                val usesVars = isVarPlaceholder(user) || isVarPlaceholder(pass)
                if (usesVars) {
                    // Keep tokens as-is; JetBrains HTTP Client will substitute variables
                    val u = if (isVarPlaceholder(user)) user else user
                    val p = if (isVarPlaceholder(pass)) pass else pass
                    "Authorization" to "Basic $u $p"
                } else {
                    val token = Base64.getEncoder().encodeToString("$user:$pass".toByteArray())
                    "Authorization" to "Basic $token"
                }
            }

            "Bearer token" -> {
                val token = bearerTokenField.text.trim()
                if (token.isEmpty()) return null
                "Authorization" to "Bearer $token"
            }

            "Digest" -> {
                val user = digestUserField.text.orEmpty().trim()
                val pass = digestPassField.text.orEmpty().trim()
                if (user.isEmpty() && pass.isEmpty()) return null
                val usesVars = isVarPlaceholder(user) || isVarPlaceholder(pass)
                if (usesVars) {
                    val u = user
                    val p = pass
                    "Authorization" to "Digest $u $p"
                } else {
                    // JetBrains HTTP Client supports shorthand; actual hashing happens on server challenge
                    "Authorization" to "Digest $user $pass"
                }
            }

            "Custom header" -> {
                val key = customKeyField.text.trim()
                val value = customValueField.text.trim()
                if (key.isEmpty() || value.isEmpty()) return null
                key to value
            }

            else -> null
        }
    }

    fun setFromHeader(headers: List<Pair<String, String>>) {
        val auth = headers.firstOrNull { it.first.equals("Authorization", true) } ?: return
        val value = auth.second

        when {
            value.startsWith("Basic ") -> {
                typeBox.selectedItem = "Basic"
                if (value.contains("{{") && value.contains("}}")) {
                    // Contains variable placeholders; cannot decode
                    val parts = value.removePrefix("Basic ").trim().split(" ", limit = 2)
                    basicUserField.text = parts.getOrNull(0) ?: ""
                    basicPassField.text = parts.getOrNull(1) ?: ""
                    (cards.layout as CardLayout).show(cards, typeBox.selectedItem as String)
                    return
                }
                val decoded = try {
                    String(Base64.getDecoder().decode(value.removePrefix("Basic ").trim()))
                } catch (_: Exception) {
                    ""
                }
                val parts = decoded.split(":", limit = 2)
                basicUserField.text = parts.getOrNull(0) ?: ""
                basicPassField.text = parts.getOrNull(1) ?: ""
            }

            value.startsWith("Bearer ") -> {
                typeBox.selectedItem = "Bearer token"
                bearerTokenField.text = value.removePrefix("Bearer ").trim()
            }

            value.startsWith("Digest ") -> {
                typeBox.selectedItem = "Digest"
                val creds = value.removePrefix("Digest ").trim().split(" ")
                digestUserField.text = creds.getOrNull(0) ?: ""
                digestPassField.text = creds.getOrNull(1) ?: ""
            }

            else -> {
                typeBox.selectedItem = "Custom header"
                customKeyField.text = auth.first
                customValueField.text = auth.second
            }
        }
        (cards.layout as CardLayout).show(cards, typeBox.selectedItem as String)
    }

    fun clear() {
        typeBox.selectedItem = "None"
        basicUserField.text = ""
        basicPassField.text = ""
        bearerTokenField.text = ""
        digestUserField.text = ""
        digestPassField.text = ""
        customKeyField.text = ""
        customValueField.text = ""
        (cards.layout as CardLayout).show(cards, "None")
    }

}
