package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Project
import com.example.data.ProjectRepository
import com.example.data.WebPlugin
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class EditorTab {
    HTML, CSS, JS
}

enum class EditorTheme {
    DARK_MONOCHROME, // Charcoal / Silver
    OBSIDIAN_DRACULA, // Dracula palette (Purple/Pink/Dark-gray)
    COBALT_DEEP, // Blue / Cyan / Gold
    CYBERPUNK, // Neon-green / Magenta / Yellow / Black
    LIGHT_SAND // Warm Light
}

class EditorViewModel(
    application: Application,
    private val repository: ProjectRepository
) : AndroidViewModel(application) {

    // List of all projects
    val allProjects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Selection & State
    private val _currentProject = MutableStateFlow<Project?>(null)
    val currentProject = _currentProject.asStateFlow()

    // Screen states
    private val _activeTab = MutableStateFlow(EditorTab.HTML)
    val activeTab = _activeTab.asStateFlow()

    // Temporary values for editing (to avoid thrashing db constantly)
    private val _htmlBuffer = MutableStateFlow("")
    val htmlBuffer = _htmlBuffer.asStateFlow()

    private val _cssBuffer = MutableStateFlow("")
    val cssBuffer = _cssBuffer.asStateFlow()

    private val _jsBuffer = MutableStateFlow("")
    val jsBuffer = _jsBuffer.asStateFlow()

    // Preview
    private val _isLivePreviewEnabled = MutableStateFlow(true)
    val isLivePreviewEnabled = _isLivePreviewEnabled.asStateFlow()

    private val _previewTrigger = MutableStateFlow(0)
    val previewTrigger = _previewTrigger.asStateFlow()

    // Theme state
    private val _editorTheme = MutableStateFlow(EditorTheme.OBSIDIAN_DRACULA)
    val editorTheme = _editorTheme.asStateFlow()

    // Text selection indices (for inserting snippets)
    var htmlSelection = 0
    var cssSelection = 0
    var jsSelection = 0

    // Console logs recorded from WebChromeClient
    private val _consoleLogs = MutableStateFlow<List<String>>(emptyList())
    val consoleLogs = _consoleLogs.asStateFlow()

    // ----------------------------------------------------
    // PLUGINS SYSTEM SECTION START 🔌
    // ----------------------------------------------------
    val presetPlugins = listOf(
        WebPlugin("tailwind", "Tailwind CSS CDN", "Supercharge your HTML with helper utility CSS classes directly in tags.", "Styling Framework", "https://cdn.tailwindcss.com", isScript = true),
        WebPlugin("bootstrap", "Bootstrap UI CDN", "Load the world's most popular grid layouts and pre-styled CSS elements.", "Styling Framework", "https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css", isScript = false),
        WebPlugin("animatecss", "Animate.css Dynamics", "Cross-browser micro-interactions and transitions for dynamic responsive layouts.", "Animations", "https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css", isScript = false),
        WebPlugin("sweetalert", "SweetAlert2 Pops", "Replace default browser alert() popups with highly polished, interactive modals.", "UI Alerts", "https://cdn.jsdelivr.net/npm/sweetalert2@11", isScript = true),
        WebPlugin("fontawesome", "FontAwesome Icons", "Access thousands of scalable icon vectors to highlight items, buttons, or navbars.", "Icons Suite", "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css", isScript = false),
        WebPlugin("jquery", "jQuery Library", "Classic, versatile DOM selector framework for simple click-events and animations.", "Core Utilities", "https://code.jquery.com/jquery-3.6.0.min.js", isScript = true),
        WebPlugin("chartjs", "Chart.js Visualizer", "Incredibly flexible HTML5 Canvas chart renderer for line, pie, and bar analytics.", "Charts", "https://cdn.jsdelivr.net/npm/chart.js", isScript = true),
        WebPlugin("threejs", "Three.js 3D Engine", "High performance, lightweight WebGL rendering framework to host 3D scenes in-app.", "3D Graphics", "https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js", isScript = true),
        WebPlugin("animejs", "Anime.js Sequencer", "Choreograph complex HTML animations with an elegant timeline-based syntax.", "Animations", "https://cdnjs.cloudflare.com/ajax/libs/animejs/3.2.1/anime.min.js", isScript = true),
        WebPlugin("cairofont", "Cairo & Poppins Fonts", "Binds Cairo (magnificent Arabic text) and Poppins (clean English) web fonts.", "Typography", "https://fonts.googleapis.com/css2?family=Cairo:wght@400;700&family=Poppins:wght@300;600;800&display=swap", isScript = false)
    )

    private val _projectPlugins = MutableStateFlow<List<WebPlugin>>(emptyList())
    val projectPlugins = _projectPlugins.asStateFlow()

    fun parseCustomPlugins(raw: String): List<WebPlugin> {
        if (raw.isBlank()) return emptyList()
        val list = mutableListOf<WebPlugin>()
        try {
            val items = raw.split("|~|")
            for (item in items) {
                if (item.isBlank()) continue
                val parts = item.split("^~^")
                if (parts.size >= 5) {
                    list.add(
                        WebPlugin(
                            id = parts[0],
                            name = parts[1],
                            description = parts[2],
                            category = parts[3],
                            url = parts[4],
                            isScript = parts.getOrNull(5)?.toBoolean() ?: true,
                            isCustom = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun serializeCustomPlugins(list: List<WebPlugin>): String {
        return list.filter { it.isCustom }.joinToString("|~|") { p ->
            "${p.id}^~^${p.name}^~^${p.description}^~^${p.category}^~^${p.url}^~^${p.isScript}"
        }
    }

    private fun rebuildPluginsList(project: Project) {
        val enabledSet = project.enabledPluginIds.split(",").filter { it.isNotBlank() }.toSet()
        val customList = parseCustomPlugins(project.customPluginsRaw)
        val combined = mutableListOf<WebPlugin>()
        
        for (preset in presetPlugins) {
            combined.add(preset.copy(isEnabled = enabledSet.contains(preset.id)))
        }
        for (custom in customList) {
            combined.add(custom.copy(isEnabled = enabledSet.contains(custom.id)))
        }
        _projectPlugins.value = combined
    }

    fun togglePlugin(pluginId: String) {
        val current = _currentProject.value ?: return
        val enabledSet = current.enabledPluginIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        
        if (enabledSet.contains(pluginId)) {
            enabledSet.remove(pluginId)
        } else {
            enabledSet.add(pluginId)
        }
        
        val updatedEnabledString = enabledSet.joinToString(",")
        val updatedProject = current.copy(
            enabledPluginIds = updatedEnabledString,
            lastModifiedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        rebuildPluginsList(updatedProject)
        
        viewModelScope.launch {
            repository.updateProject(updatedProject)
        }
        
        if (_isLivePreviewEnabled.value) {
            triggerManualPreviewRun()
        }
    }

    fun addCustomPlugin(name: String, desc: String, category: String, url: String, isScript: Boolean) {
        val current = _currentProject.value ?: return
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) return
        
        val id = "custom_" + System.currentTimeMillis()
        val currentCustom = parseCustomPlugins(current.customPluginsRaw).toMutableList()
        val newPlugin = WebPlugin(
            id = id,
            name = name.trim().ifBlank { "External Library" },
            description = desc.trim().ifBlank { "External imported resource" },
            category = category.trim().ifBlank { "Custom" },
            url = cleanUrl,
            isScript = isScript,
            isCustom = true,
            isEnabled = true
        )
        currentCustom.add(newPlugin)
        
        val updatedCustomRaw = serializeCustomPlugins(currentCustom)
        val enabledSet = current.enabledPluginIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        enabledSet.add(id)
        
        val updatedProject = current.copy(
            customPluginsRaw = updatedCustomRaw,
            enabledPluginIds = enabledSet.joinToString(","),
            lastModifiedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        rebuildPluginsList(updatedProject)
        
        viewModelScope.launch {
            repository.updateProject(updatedProject)
        }
        
        if (_isLivePreviewEnabled.value) {
            triggerManualPreviewRun()
        }
    }

    fun deleteCustomPlugin(pluginId: String) {
        val current = _currentProject.value ?: return
        val currentCustom = parseCustomPlugins(current.customPluginsRaw).toMutableList()
        currentCustom.removeAll { it.id == pluginId }
        
        val updatedCustomRaw = serializeCustomPlugins(currentCustom)
        val enabledSet = current.enabledPluginIds.split(",").filter { it.isNotBlank() }.toMutableSet()
        enabledSet.remove(pluginId)
        
        val updatedProject = current.copy(
            customPluginsRaw = updatedCustomRaw,
            enabledPluginIds = enabledSet.joinToString(","),
            lastModifiedAt = System.currentTimeMillis()
        )
        _currentProject.value = updatedProject
        rebuildPluginsList(updatedProject)
        
        viewModelScope.launch {
            repository.updateProject(updatedProject)
        }
        
        if (_isLivePreviewEnabled.value) {
            triggerManualPreviewRun()
        }
    }
    // ----------------------------------------------------
    // PLUGINS SYSTEM SECTION END 🔌
    // ----------------------------------------------------

    private var autoSaveJob: Job? = null

    init {
        // Seed initial templates if they don't exist
        viewModelScope.launch {
            repository.allProjects.collect { projects ->
                if (projects.isEmpty()) {
                    seedDefaultTemplates()
                } else if (_currentProject.value == null) {
                    // Automatically load the first/newest project
                    loadProject(projects.first())
                }
            }
        }
    }

    private suspend fun seedDefaultTemplates() {
        val templates = listOf(
            Project(
                name = "Interactive Starter",
                html = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Hello World</title>
</head>
<body>
    <div class="card">
        <h1>HTML Live Editor 🚀</h1>
        <p>Edit HTML, CSS, and JS tabs. Watch changes reload instantly!</p>
        <div id="counter">Taps: <span id="count">0</span></div>
        <button id="btn">Click Me!</button>
    </div>
</body>
</html>""",
                css = """body {
    font-family: 'Segoe UI', system-ui, sans-serif;
    background: linear-gradient(135deg, #121214, #1a1a2e);
    color: #e4e4eb;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 90vh;
    margin: 0;
    padding: 15px;
}
.card {
    background-color: #24252d;
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 16px;
    padding: 2.5rem;
    text-align: center;
    max-width: 400px;
    box-shadow: 0 10px 30px rgba(0,0,0,0.4);
}
h1 {
    color: #00d2ff;
    margin-top: 0;
    font-size: 2rem;
}
p {
    color: #b3b3bf;
    line-height: 1.6;
}
#counter {
    font-size: 1.2rem;
    font-weight: bold;
    color: #ffd700;
    margin: 20px 0;
}
button {
    background-color: #00d2ff;
    color: #121214;
    border: none;
    padding: 0.8rem 1.8rem;
    font-size: 1rem;
    font-weight: bold;
    border-radius: 8px;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(0, 210, 255, 0.3);
    transition: transform 0.2s, background-color 0.2s;
}
button:active {
    transform: scale(0.95);
}""",
                js = """// Web Interactive Live Script
const button = document.getElementById('btn');
const countElement = document.getElementById('count');
let count = 0;

button.addEventListener('click', () => {
    count++;
    countElement.textContent = count;
    console.log('Button was clicked. New count: ' + count);
    
    // Play with bubble alert effects on milestone
    if (count % 5 === 0) {
        console.log('Milestone reached! 5 clicks!');
    }
});

console.clear();
console.log('Ready to edit! Make changes above to see updates here.');"""
            ),
            Project(
                name = "Retro Tic Tac Toe",
                html = """<!DOCTYPE html>
<html lang="en">
<head>
    <title>Retro Tic Tac Toe</title>
</head>
<body>
    <h1>Tic Tac Toe</h1>
    <div id="status">Player X's turn</div>
    <div class="board" id="board">
        <div class="cell" data-index="0"></div>
        <div class="cell" data-index="1"></div>
        <div class="cell" data-index="2"></div>
        <div class="cell" data-index="3"></div>
        <div class="cell" data-index="4"></div>
        <div class="cell" data-index="5"></div>
        <div class="cell" data-index="6"></div>
        <div class="cell" data-index="7"></div>
        <div class="cell" data-index="8"></div>
    </div>
    <button id="reset-btn">Reset Board</button>
</body>
</html>""",
                css = """body {
    font-family: system-ui, sans-serif;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background-color: #0b0c10;
    color: #fff;
    margin: 0;
    padding: 20px;
    min-height: 90vh;
}
h1 {
    margin: 10px 0;
    color: #66fcf1;
    font-size: 2.2rem;
    letter-spacing: 2px;
}
#status {
    font-size: 1.3rem;
    margin-bottom: 20px;
    color: #c5c6c7;
    font-weight: 500;
}
.board {
    display: grid;
    grid-template-columns: repeat(3, 85px);
    grid-template-rows: repeat(3, 85px);
    gap: 12px;
    margin-bottom: 25px;
}
.cell {
    background-color: #1f2833;
    border: 2px solid #45f3ff;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 2.5rem;
    font-weight: 800;
    cursor: pointer;
    box-shadow: 0 4px 6px rgba(0,0,0,0.3);
    transition: all 0.2s ease;
}
.cell:active {
    transform: scale(0.92);
}
button {
    background-color: #66fcf1;
    color: #0b0c10;
    border: none;
    padding: 12px 24px;
    font-size: 1.1rem;
    font-weight: bold;
    border-radius: 8px;
    cursor: pointer;
    box-shadow: 0 4px 14px rgba(102, 252, 241, 0.4);
}
button:active {
    transform: scale(0.95);
}""",
                js = """const cells = document.querySelectorAll('.cell');
const statusText = document.getElementById('status');
const resetBtn = document.getElementById('reset-btn');
let currentPlayer = 'X';
let gameActive = true;
let gameState = ["", "", "", "", "", "", "", "", ""];

const winningConditions = [
    [0, 1, 2], [3, 4, 5], [6, 7, 8],
    [0, 3, 6], [1, 4, 7], [2, 5, 8],
    [0, 4, 8], [2, 4, 6]
];

function handleCellClick(e) {
    const cell = e.target;
    const index = parseInt(cell.getAttribute('data-index'));
    if (gameState[index] !== "" || !gameActive) return;

    gameState[index] = currentPlayer;
    cell.textContent = currentPlayer;
    cell.style.color = currentPlayer === 'X' ? '#66fcf1' : '#ff007f';
    console.log('Cell ' + index + ' marked with ' + currentPlayer);
    checkResult();
}

function checkResult() {
    let roundWon = false;
    for (let condition of winningConditions) {
        const [a, b, c] = condition;
        if (gameState[a] && gameState[a] === gameState[b] && gameState[a] === gameState[c]) {
            roundWon = true;
            break;
        }
    }
    if (roundWon) {
        statusText.textContent = 'Player ' + currentPlayer + ' Wins! 🎉';
        statusText.style.color = '#ffd700';
        console.log('Match won by Player ' + currentPlayer);
        gameActive = false;
        return;
    }
    if (!gameState.includes("")) {
        statusText.textContent = 'Draw! 🤝';
        console.log('Match ended in a Draw');
        gameActive = false;
        return;
    }
    currentPlayer = currentPlayer === 'X' ? 'O' : 'X';
    statusText.textContent = 'Player ' + currentPlayer + "'s turn";
}

function resetGame() {
    currentPlayer = 'X';
    gameActive = true;
    gameState = ["", "", "", "", "", "", "", "", ""];
    statusText.textContent = "Player X's turn";
    statusText.style.color = '#fff';
    cells.forEach(cell => {
        cell.textContent = "";
    });
    console.clear();
    console.log("New game started!");
}

cells.forEach(cell => cell.addEventListener('click', handleCellClick));
resetBtn.addEventListener('click', resetGame);
console.log("Classic Tic Tac Toe Initialised!");"""
            ),
            Project(
                name = "Physics Canvas Engine",
                html = """<!DOCTYPE html>
<html>
<head>
    <title>Canvas Dynamics</title>
</head>
<body>
    <h2>Gravity Bubble Canvas</h2>
    <canvas id="gravityCanvas"></canvas>
    <div class="metrics">
        Tap to throw particles. Particle Count: <span id="pCount">0</span>
    </div>
</body>
</html>""",
                css = """body {
    margin: 0;
    padding: 10px;
    background: #0d0e15;
    color: #e1e1e6;
    font-family: monospace;
    display: flex;
    flex-direction: column;
    align-items: center;
    max-height: 100vh;
}
h2 {
    margin: 10px 0;
    color: #f7a3ff;
    font-size: 1.3rem;
    letter-spacing: 1px;
}
canvas {
    border: 1px solid rgba(247, 163, 255, 0.3);
    border-radius: 12px;
    background: #020204;
    max-width: 100%;
}
.metrics {
    color: #88c0d0;
    margin-top: 10px;
    font-size: 0.85rem;
}""",
                js = """const canvas = document.getElementById('gravityCanvas');
const ctx = canvas.getContext('2d');
const pCountText = document.getElementById('pCount');

// Handle sizing dynamically
canvas.width = window.innerWidth * 0.9;
canvas.height = window.innerHeight * 0.55;

let particles = [];
const gravity = 0.35;
const friction = 0.82;

class Particle {
    constructor(x, y) {
        this.x = x;
        this.y = y;
        this.radius = Math.random() * 15 + 6;
        this.vx = Math.random() * 10 - 5;
        this.vy = Math.random() * -8 - 4; // throw upwards
        this.color = 'hsl(' + Math.random() * 360 + ', 95%, 65%)';
    }
    update() {
        this.vy += gravity;
        this.x += this.vx;
        this.y += this.vy;

        // Bottom bounce
        if (this.y + this.radius > canvas.height) {
            this.y = canvas.height - this.radius;
            this.vy = -this.vy * friction;
            this.vx = this.vx * friction;
        }

        // Left/Right bounce
        if (this.x - this.radius < 0) {
            this.x = this.radius;
            this.vx = -this.vx * friction;
        } else if (this.x + this.radius > canvas.width) {
            this.x = canvas.width - this.radius;
            this.vx = -this.vx * friction;
        }
    }
    draw() {
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
        ctx.fillStyle = this.color;
        ctx.shadowColor = this.color;
        ctx.shadowBlur = 10;
        ctx.fill();
        ctx.shadowBlur = 0; // reset
    }
}

function loop() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    
    particles.forEach((p, idx) => {
        p.update();
        p.draw();
        
        // Remove particles that run out of energy and sit on floor
        if (Math.abs(p.vy) < 0.2 && p.y + p.radius >= canvas.height - 1 && Math.abs(p.vx) < 0.1) {
            particles.splice(idx, 1);
        }
    });

    pCountText.textContent = particles.length;
    requestAnimationFrame(loop);
}

canvas.addEventListener('click', (e) => {
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    console.log('Creating bubbles at ('+Math.round(x)+', '+Math.round(y)+')');
    for (let i = 0; i < 12; i++) {
        particles.push(new Particle(x, y));
    }
});

// Seed Initial set
for(let i=0; i<8; i++){
    particles.push(new Particle(canvas.width/2, canvas.height * 0.4));
}

loop();
console.log('Gravity Sandbox Online!');"""
            )
        )
        for (tpl in templates) {
            repository.insertProject(tpl)
        }
    }

    fun loadProject(project: Project) {
        _currentProject.value = project
        _htmlBuffer.value = project.html
        _cssBuffer.value = project.css
        _jsBuffer.value = project.js
        _consoleLogs.value = emptyList() // Reset console
        rebuildPluginsList(project)
    }

    fun selectTab(tab: EditorTab) {
        _activeTab.value = tab
    }

    fun updateHtml(code: String) {
        _htmlBuffer.value = code
        triggerAutoSave()
    }

    fun updateCss(code: String) {
        _cssBuffer.value = code
        triggerAutoSave()
    }

    fun updateJs(code: String) {
        _jsBuffer.value = code
        triggerAutoSave()
    }

    fun setLivePreviewEnabled(enabled: Boolean) {
        _isLivePreviewEnabled.value = enabled
    }

    fun triggerManualPreviewRun() {
        _previewTrigger.value = _previewTrigger.value + 1
    }

    fun setEditorTheme(theme: EditorTheme) {
        _editorTheme.value = theme
    }

    fun addConsoleLog(log: String) {
        // Limit logs to last 150 items to protect memory
        val current = _consoleLogs.value.toMutableList()
        current.add(log)
        if (current.size > 150) {
            current.removeAt(0)
        }
        _consoleLogs.value = current
    }

    fun clearConsoleLogs() {
        _consoleLogs.value = emptyList()
    }

    fun createNewProject(name: String) {
        viewModelScope.launch {
            val fresh = Project(
                name = name.ifBlank { "Untitled Project" },
                html = "<h1>New Project</h1>\n<p>Start writing elements here...</p>",
                css = "body {\n  font-family: sans-serif;\n  padding: 20px;\n}",
                js = "console.log('New Canvas Loaded');"
            )
            val newId = repository.insertProject(fresh)
            delay(100)
            val inserted = repository.getProjectById(newId.toInt())
            if (inserted != null) {
                loadProject(inserted)
            }
        }
    }

    fun duplicateCurrentProject() {
        val proj = _currentProject.value ?: return
        viewModelScope.launch {
            val copy = Project(
                name = "${proj.name} (Copy)",
                html = _htmlBuffer.value,
                css = _cssBuffer.value,
                js = _jsBuffer.value
            )
            val newId = repository.insertProject(copy)
            delay(100)
            val inserted = repository.getProjectById(newId.toInt())
            if (inserted != null) {
                loadProject(inserted)
            }
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            val projects = allProjects.value
            repository.deleteProject(project)
            
            // If the deleted project was the currently loaded one
            if (_currentProject.value?.id == project.id) {
                val remaining = projects.filter { it.id != project.id }
                if (remaining.isNotEmpty()) {
                    loadProject(remaining.first())
                } else {
                    _currentProject.value = null
                    _htmlBuffer.value = ""
                    _cssBuffer.value = ""
                    _jsBuffer.value = ""
                    _consoleLogs.value = emptyList()
                }
            }
        }
    }

    fun saveCurrentProjectStateSilently() {
        val current = _currentProject.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                html = _htmlBuffer.value,
                css = _cssBuffer.value,
                js = _jsBuffer.value,
                lastModifiedAt = System.currentTimeMillis(),
                enabledPluginIds = current.enabledPluginIds,
                customPluginsRaw = current.customPluginsRaw
            )
            repository.updateProject(updated)
            _currentProject.value = updated
        }
    }

    private fun triggerAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(1500) // Debounce auto-saves for 1.5s
            saveCurrentProjectStateSilently()
        }
    }

    fun getCombinedHtmlOutput(): String {
        val htmlBody = _htmlBuffer.value
        val cssBody = _cssBuffer.value
        val jsBody = _jsBuffer.value

        val pluginsHtml = _projectPlugins.value
            .filter { it.isEnabled }
            .joinToString("\n") { it.toHtmlTag() }

        // Setup base document wrapper
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                $pluginsHtml
                <style>
                    $cssBody
                </style>
                <script>
                    // Overwrite default console.log to communicate back to our client
                    (function() {
                        const originalLog = console.log;
                        const originalError = console.error;
                        const originalClear = console.clear;
                        const originalInfo = console.info;

                        console.log = function(...args) {
                            originalLog.apply(console, args);
                            const msg = args.map(arg => typeof arg === 'object' ? JSON.stringify(arg) : arg).join(' ');
                            if (window.AndroidConsole) {
                                window.AndroidConsole.logMessage('LOG: ' + msg);
                            }
                        };
                        console.error = function(...args) {
                            originalError.apply(console, args);
                            const msg = args.map(arg => typeof arg === 'object' ? JSON.stringify(arg) : arg).join(' ');
                            if (window.AndroidConsole) {
                                window.AndroidConsole.logMessage('ERROR: ' + msg);
                            }
                        };
                        console.info = function(...args) {
                            originalInfo.apply(console, args);
                            const msg = args.map(arg => typeof arg === 'object' ? JSON.stringify(arg) : arg).join(' ');
                            if (window.AndroidConsole) {
                                window.AndroidConsole.logMessage('INFO: ' + msg);
                            }
                        };
                        console.clear = function() {
                            originalClear.apply(console);
                            if (window.AndroidConsole) {
                                window.AndroidConsole.clearLogs();
                            }
                        };

                        window.onerror = function(message, source, lineno, colno, error) {
                            const errStr = message + ' (line ' + lineno + ')';
                            if (window.AndroidConsole) {
                                window.AndroidConsole.logMessage('RUNTIME ERROR: ' + errStr);
                            }
                            return false;
                        };
                    })();
                </script>
            </head>
            <body>
                $htmlBody
                <script>
                    try {
                        $jsBody
                    } catch(e) {
                        console.error(e.message);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
    }

    fun insertTextAtSelection(insertText: String) {
        when (_activeTab.value) {
            EditorTab.HTML -> {
                val current = _htmlBuffer.value
                val index = htmlSelection.coerceIn(0, current.length)
                val updated = current.substring(0, index) + insertText + current.substring(index)
                _htmlBuffer.value = updated
                htmlSelection = index + insertText.length
            }
            EditorTab.CSS -> {
                val current = _cssBuffer.value
                val index = cssSelection.coerceIn(0, current.length)
                val updated = current.substring(0, index) + insertText + current.substring(index)
                _cssBuffer.value = updated
                cssSelection = index + insertText.length
            }
            EditorTab.JS -> {
                val current = _jsBuffer.value
                val index = jsSelection.coerceIn(0, current.length)
                val updated = current.substring(0, index) + insertText + current.substring(index)
                _jsBuffer.value = updated
                jsSelection = index + insertText.length
            }
        }
        triggerAutoSave()
    }

    fun formatSelectedBuffer() {
        when (_activeTab.value) {
            EditorTab.HTML -> {
                val lines = _htmlBuffer.value.lines()
                val formatted = formatLinesSimple(lines)
                _htmlBuffer.value = formatted
            }
            EditorTab.CSS -> {
                val lines = _cssBuffer.value.lines()
                val formatted = formatLinesSimple(lines)
                _cssBuffer.value = formatted
            }
            EditorTab.JS -> {
                val lines = _jsBuffer.value.lines()
                val formatted = formatLinesSimple(lines)
                _jsBuffer.value = formatted
            }
        }
        triggerAutoSave()
    }

    private fun formatLinesSimple(lines: List<String>): String {
        var indentLevel = 0
        val spaceIndent = "  "
        val clean = lines.map { it.trim() }
        val sb = StringBuilder()

        for (line in clean) {
            if (line.isEmpty()) {
                sb.append("\n")
                continue
            }

            // Decrease indent before this line if it's a closing bracket
            val closeCount = line.count { it == '}' || it == ']' || it == ')' } + if (line.startsWith("</") || line.startsWith("}")) 1 else 0
            if (closeCount > 0) {
                indentLevel = (indentLevel - closeCount).coerceAtLeast(0)
            }

            // Apply indentation
            for (i in 0 until indentLevel) {
                sb.append(spaceIndent)
            }
            sb.append(line).append("\n")

            // Increase indent after this line if it opens brackets
            val openCount = line.count { it == '{' || it == '[' || it == '(' } + if (line.contains("<") && !line.contains("</") && !line.contains("/>") && !line.startsWith("<!")) {
                // simple tag depth check
                val t = line.substringAfter("<").substringBefore(" ").substringBefore(">")
                if (t.isNotBlank() && !listOf("img", "br", "input", "hr", "meta", "link").contains(t.lowercase())) 1 else 0
            } else 0
            if (openCount > 0) {
                indentLevel += openCount
            }
        }
        return sb.toString().trim()
    }

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}

class EditorViewModelFactory(
    private val application: Application,
    private val repository: ProjectRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
