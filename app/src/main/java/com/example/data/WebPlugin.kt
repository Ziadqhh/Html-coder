package com.example.data

data class WebPlugin(
    val id: String,
    val name: String,
    val description: String,
    val category: String, // e.g., "Styling", "Animations", "Icons", "Charts", "Custom"
    val url: String,
    val isScript: Boolean, // True for .js or <script>, False for .css or <style>
    val isCustom: Boolean = false,
    val isEnabled: Boolean = false
) {
    // Generates the appropriate HTML element tags for injecting this plugin into the preview
    fun toHtmlTag(): String {
        return if (isScript) {
            if (url.startsWith("http")) {
                "<script src=\"$url\" defer></script>"
            } else {
                "<script>\n$url\n</script>"
            }
        } else {
            if (url.startsWith("http")) {
                "<link rel=\"stylesheet\" href=\"$url\">"
            } else {
                "<style>\n$url\n</style>"
            }
        }
    }
}
