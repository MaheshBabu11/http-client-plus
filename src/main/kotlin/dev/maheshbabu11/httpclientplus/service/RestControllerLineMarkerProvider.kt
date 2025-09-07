package dev.maheshbabu11.httpclientplus.service

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.CommonClassNames
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil

class RestControllerLineMarkerProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val method = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return null
        if (element != method.nameIdentifier) return null
        val annotation = method.annotations.find { ann ->
            ann.qualifiedName?.endsWith("Mapping") == true
        } ?: return null

        val info = LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.Execute,
            { "Add request to HTTP Client Plus" },
            { _, elt ->
                val project = elt.project
                val request = buildRequestFromAnnotation(method, annotation)
                HttpClientPlusService.getInstance(project).openRequest(request)
            },
            GutterIconRenderer.Alignment.LEFT,
            { "Add to HTTP Client Plus" }
        )
        NavigateAction.setNavigateAction(info, "Add to HTTP Client Plus", null)
        return info
    }

    fun buildRequestFromAnnotation(method: PsiMethod, ann: PsiElement): HttpRequestData {
        val httpMethod = when {
            ann.text.contains("GetMapping") -> "GET"
            ann.text.contains("PostMapping") -> "POST"
            ann.text.contains("PutMapping") -> "PUT"
            ann.text.contains("DeleteMapping") -> "DELETE"
            ann.text.contains("PatchMapping") -> "PATCH"
            ann.text.contains("OptionsMapping") -> "OPTIONS"
            ann.text.contains("HeadMapping") -> "HEAD"
            else -> "GET"
        }
        val containingClass = method.containingClass
        val classRequestMapping = containingClass?.annotations
            ?.find { it.qualifiedName?.endsWith("RequestMapping") == true }

        val basePath = classRequestMapping?.let { clsAnn ->
            listOf("value", "path")
                .asSequence()
                .mapNotNull { key -> clsAnn.findAttributeValue(key)?.text?.trim('"') }
                .firstOrNull()
        } ?: ""
        val annotation = ann as? PsiAnnotation
        val methodPath = listOf("value", "path")
            .asSequence()
            .mapNotNull { key -> annotation?.findAttributeValue(key)?.text?.trim('"') }
            .firstOrNull() ?: ""

        val path = (basePath.trimEnd('/') + "/" + methodPath.trimStart('/')).ifEmpty { "/" }

        val queryParams = extractQueryParams(method)
        var body: String? = null
        val bodyParam = method.parameterList.parameters.find { param ->
            param.annotations.any { it.qualifiedName?.endsWith("RequestBody") == true }
        }
        if (httpMethod in listOf("POST", "PUT", "PATCH")) {
            bodyParam?.let {
                body = generateJsonSkeleton(it.type)
            }
        }

        return HttpRequestData(
            method = httpMethod,
            url = path + if (queryParams != null) "?$queryParams" else "",
            name = method.name,
            body = body
        )
    }

    private fun generateJsonSkeleton(type: PsiType): String {
        return when {
            type.equalsToText(CommonClassNames.JAVA_LANG_STRING) -> "\"string\""
            type.equalsToText(CommonClassNames.JAVA_LANG_INTEGER) ||
                    type.equalsToText("int") ||
                    type.equalsToText(CommonClassNames.JAVA_LANG_LONG) ||
                    type.equalsToText("long") -> "0"

            type.equalsToText(CommonClassNames.JAVA_LANG_BOOLEAN) ||
                    type.equalsToText("boolean") -> "false"

            type is PsiArrayType -> "[${generateJsonSkeleton(type.componentType)}]"
            type is PsiClassType -> {
                val psiClass = type.resolve()
                if (psiClass != null && psiClass.isEnum) {
                    "\"${psiClass.fields.firstOrNull { it is PsiEnumConstant }?.name ?: "ENUM"}\""
                } else if (psiClass != null) {
                    val fields = psiClass.allFields
                        .filter { !it.hasModifierProperty(PsiModifier.STATIC) }
                        .associate { field ->
                            field.name to generateJsonSkeleton(field.type)
                        }
                    fields.entries.joinToString(
                        prefix = "{", postfix = "}"
                    ) { (name, value) -> "\"$name\": $value" }
                } else {
                    "{}"
                }
            }

            else -> "null"
        }
    }

    private fun extractQueryParams(method: PsiMethod): String? {
        val params = method.parameterList.parameters
            .filter { param ->
                param.annotations.any { it.qualifiedName?.endsWith("RequestParam") == true }
            }
            .map { param ->
                val annotation = param.annotations
                    .find { it.qualifiedName?.endsWith("RequestParam") == true }

                val explicitName = listOf("value", "name")
                    .asSequence()
                    .mapNotNull { attr ->
                        annotation?.findAttributeValue(attr)?.text?.trim('"')
                    }
                    .firstOrNull()

                // If no explicit name, extract from source text
                val key = explicitName.takeUnless { it.isNullOrEmpty() }
                    ?: param.name
                    ?: param.text.split("\\s+".toRegex()).lastOrNull()
                    ?: "param"

                "$key="
            }

        return if (params.isNotEmpty()) params.joinToString("&") else null
    }

    private fun extractPathVariables(method: PsiMethod): List<String> {
        return method.parameterList.parameters
            .filter { param ->
                param.annotations.any { it.qualifiedName?.endsWith("PathVariable") == true }
            }
            .map { param ->
                val annotation = param.annotations
                    .find { it.qualifiedName?.endsWith("PathVariable") == true }

                val explicitName = listOf("value", "name")
                    .asSequence()
                    .mapNotNull { attr ->
                        annotation?.findAttributeValue(attr)?.text?.trim('"')
                    }
                    .firstOrNull()

                explicitName.takeUnless { it.isNullOrEmpty() }
                    ?: param.name
                    ?: param.text.split("\\s+".toRegex()).lastOrNull()
                    ?: "param"
            }
    }

}
