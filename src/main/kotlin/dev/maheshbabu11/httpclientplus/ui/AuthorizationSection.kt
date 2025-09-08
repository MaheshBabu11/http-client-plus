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

    companion object {
        private const val TYPE_NONE = "None"
        private const val TYPE_BASIC = "Basic"
        private const val TYPE_BEARER = "Bearer token"
        private const val TYPE_DIGEST = "Digest"
        private const val TYPE_CUSTOM = "Custom header"

        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val PREFIX_BASIC = "Basic "
        private const val PREFIX_BEARER = "Bearer "
        private const val PREFIX_DIGEST = "Digest "
    }

    private val typeBox = ComboBox(
        arrayOf(
            TYPE_NONE,
            TYPE_BASIC,
            TYPE_BEARER,
            TYPE_DIGEST,
            TYPE_CUSTOM
        )
    )

    private val basicUserField = JBTextField()
    private val basicPassField = JBTextField()

    private val bearerTokenField = JBTextField()

    private val digestUserField = JBTextField()
    private val digestPassField = JBTextField()

    private val customKeyField = JBTextField(HEADER_AUTHORIZATION)
    private val customValueField = JBTextField()

    private val cards = JPanel(CardLayout())

    val component: JComponent = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)

        val typeBoxPreferredHeight = 32
        val typeBoxPreferredWidth = 220

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
        cards.add(buildNonePanel(), TYPE_NONE)
        cards.add(buildBasicPanel(), TYPE_BASIC)
        cards.add(buildBearerPanel(), TYPE_BEARER)
        cards.add(buildDigestPanel(), TYPE_DIGEST)
        cards.add(buildCustomPanel(), TYPE_CUSTOM)

        val content = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty()

            top.alignmentX = JComponent.LEFT_ALIGNMENT
            add(top)
            add(Box.createVerticalStrut(8))

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
            TYPE_NONE -> null
            TYPE_BASIC -> {
                val user = basicUserField.text.orEmpty().trim()
                val pass = basicPassField.text.orEmpty().trim()
                if (user.isEmpty() && pass.isEmpty()) return null
                val usesVars = isVarPlaceholder(user) || isVarPlaceholder(pass)
                if (usesVars) {
                    HEADER_AUTHORIZATION to "${PREFIX_BASIC.trim()} $user $pass"
                } else {
                    val token = Base64.getEncoder().encodeToString("$user:$pass".toByteArray())
                    HEADER_AUTHORIZATION to "$PREFIX_BASIC$token"
                }
            }

            TYPE_BEARER -> {
                val token = bearerTokenField.text.trim()
                if (token.isEmpty()) return null
                HEADER_AUTHORIZATION to "$PREFIX_BEARER$token"
            }

            TYPE_DIGEST -> {
                val user = digestUserField.text.orEmpty().trim()
                val pass = digestPassField.text.orEmpty().trim()
                if (user.isEmpty() && pass.isEmpty()) return null
                HEADER_AUTHORIZATION to "$PREFIX_DIGEST$user $pass"
            }

            TYPE_CUSTOM -> {
                val key = customKeyField.text.trim()
                val value = customValueField.text.trim()
                if (key.isEmpty() || value.isEmpty()) return null
                key to value
            }

            else -> null
        }
    }

    fun setFromHeader(headers: List<Pair<String, String>>) {
        val auth = headers.firstOrNull { it.first.equals(HEADER_AUTHORIZATION, true) } ?: return
        val value = auth.second

        when {
            value.startsWith(PREFIX_BASIC) -> {
                typeBox.selectedItem = TYPE_BASIC
                if (value.contains("{{") && value.contains("}}")) {
                    val parts = value.removePrefix(PREFIX_BASIC).trim().split(" ", limit = 2)
                    basicUserField.text = parts.getOrNull(0) ?: ""
                    basicPassField.text = parts.getOrNull(1) ?: ""
                } else {
                    val decoded = try {
                        String(Base64.getDecoder().decode(value.removePrefix(PREFIX_BASIC).trim()))
                    } catch (_: Exception) {
                        ""
                    }
                    val parts = decoded.split(":", limit = 2)
                    basicUserField.text = parts.getOrNull(0) ?: ""
                    basicPassField.text = parts.getOrNull(1) ?: ""
                }
            }

            value.startsWith(PREFIX_BEARER) -> {
                typeBox.selectedItem = TYPE_BEARER
                bearerTokenField.text = value.removePrefix(PREFIX_BEARER).trim()
            }

            value.startsWith(PREFIX_DIGEST) -> {
                typeBox.selectedItem = TYPE_DIGEST
                val creds = value.removePrefix(PREFIX_DIGEST).trim().split(" ")
                digestUserField.text = creds.getOrNull(0) ?: ""
                digestPassField.text = creds.getOrNull(1) ?: ""
            }

            else -> {
                typeBox.selectedItem = TYPE_CUSTOM
                customKeyField.text = auth.first
                customValueField.text = auth.second
            }
        }
        (cards.layout as CardLayout).show(cards, typeBox.selectedItem as String)
    }

    fun clear() {
        typeBox.selectedItem = TYPE_NONE
        basicUserField.text = ""
        basicPassField.text = ""
        bearerTokenField.text = ""
        digestUserField.text = ""
        digestPassField.text = ""
        customKeyField.text = ""
        customValueField.text = ""
        (cards.layout as CardLayout).show(cards, TYPE_NONE)
    }
}
