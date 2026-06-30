package com.github.ringoame196_s_mcPlugin

object MessageManager {
    private val messages = mutableMapOf<MessageKey, String>()

    fun load(data: Map<String, String>) {
        messages.clear()

        for (key in MessageKey.values()) {
            messages[key] = data[key.name] ?: continue
        }
    }

    fun get(key: MessageKey): String {
        return messages[key] ?: "No Message"
    }
}
