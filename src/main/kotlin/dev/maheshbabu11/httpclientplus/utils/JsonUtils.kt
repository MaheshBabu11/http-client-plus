package dev.maheshbabu11.httpclientplus.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

object JsonUtils {
    fun prettyPrintJson(input: String): String {
        val element = JsonParser.parseString(input)
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(element)
    }

    fun minifyJson(input: String): String {
        val element = JsonParser.parseString(input)
        val gson = GsonBuilder().create()
        return gson.toJson(element)
    }
}