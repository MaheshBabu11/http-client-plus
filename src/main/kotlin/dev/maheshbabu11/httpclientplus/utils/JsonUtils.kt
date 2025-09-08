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
