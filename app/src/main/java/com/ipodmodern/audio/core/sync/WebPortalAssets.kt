package com.ipodmodern.audio.core.sync

object WebPortalAssets {

    val INDEX_HTML = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>iPod Modern • Local Wi-Fi Sync Portal</title>
    <style>
        :root {
            --bg-body: #0d0f12;
            --card-bg: rgba(26, 29, 36, 0.85);
            --border-color: rgba(255, 255, 255, 0.1);
            --accent-gold: #ffd60a;
            --accent-blue: #0a84ff;
            --accent-green: #30d158;
            --text-main: #f5f5f7;
            --text-muted: #86868b;
        }
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
        }
        body {
            background-color: var(--bg-body);
            color: var(--text-main);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 40px 20px;
        }
        .header {
            text-align: center;
            margin-bottom: 32px;
        }
        .badge {
            display: inline-block;
            padding: 4px 12px;
            background: rgba(255, 214, 10, 0.15);
            color: var(--accent-gold);
            border: 1px solid var(--accent-gold);
            border-radius: 100px;
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 1px;
            text-transform: uppercase;
            margin-bottom: 12px;
        }
        h1 {
            font-size: 32px;
            font-weight: 700;
            letter-spacing: -0.5px;
            margin-bottom: 8px;
        }
        p.subtitle {
            color: var(--text-muted);
            font-size: 15px;
        }
        .container {
            width: 100%;
            max-width: 800px;
            display: flex;
            flex-direction: column;
            gap: 24px;
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 16px;
        }
        .stat-card {
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 20px;
            backdrop-filter: blur(20px);
        }
        .stat-label {
            font-size: 13px;
            color: var(--text-muted);
            margin-bottom: 6px;
        }
        .stat-val {
            font-size: 22px;
            font-weight: 700;
            color: var(--text-main);
        }
        .dropzone {
            background: var(--card-bg);
            border: 2px dashed rgba(255, 255, 255, 0.2);
            border-radius: 20px;
            padding: 50px 30px;
            text-align: center;
            cursor: pointer;
            transition: all 0.25s ease;
            backdrop-filter: blur(20px);
        }
        .dropzone.dragover {
            border-color: var(--accent-blue);
            background: rgba(10, 132, 255, 0.1);
        }
        .dropzone svg {
            width: 54px;
            height: 54px;
            fill: none;
            stroke: var(--accent-blue);
            stroke-width: 2;
            margin-bottom: 16px;
        }
        .btn-upload {
            background: var(--accent-blue);
            color: #fff;
            border: none;
            padding: 12px 28px;
            border-radius: 24px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            margin-top: 16px;
            transition: opacity 0.2s;
        }
        .btn-upload:hover {
            opacity: 0.9;
        }
        .file-list {
            background: var(--card-bg);
            border: 1px solid var(--border-color);
            border-radius: 16px;
            padding: 20px;
        }
        .file-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid rgba(255, 255, 255, 0.06);
        }
        .file-item:last-child {
            border-bottom: none;
        }
        .file-info {
            display: flex;
            flex-direction: column;
            gap: 4px;
        }
        .file-name {
            font-size: 14px;
            font-weight: 600;
        }
        .file-meta {
            font-size: 12px;
            color: var(--text-muted);
        }
        .progress-bar {
            width: 100%;
            height: 6px;
            background: rgba(255, 255, 255, 0.1);
            border-radius: 3px;
            overflow: hidden;
            margin-top: 8px;
        }
        .progress-fill {
            height: 100%;
            background: var(--accent-green);
            width: 0%;
            transition: width 0.2s;
        }
    </style>
</head>
<body>
    <div class="header">
        <div class="badge">Audiophile Bit-Perfect Sync</div>
        <h1>iPod Modern Sync Portal</h1>
        <p class="subtitle">Wireless High-Resolution Local Audio & CUE Sheet Transfer</p>
    </div>

    <div class="container">
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-label">Device Storage Free</div>
                <div class="stat-val" id="storageFree">Loading...</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Indexed Tracks</div>
                <div class="stat-val" id="trackCount">0</div>
            </div>
            <div class="stat-card">
                <div class="stat-label">Audio HAL Mode</div>
                <div class="stat-val" style="color: var(--accent-gold);">Exclusive Bit-Perfect</div>
            </div>
        </div>

        <div class="dropzone" id="dropzone">
            <svg viewBox="0 0 24 24"><path d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"/></svg>
            <h3>Drag & Drop Lossless Audio Files Here</h3>
            <p style="color: var(--text-muted); font-size: 13px; margin-top: 6px;">Supports FLAC, ALAC, WAV, AIFF, DSD (.dsf/.dff), MP3 & .CUE sheets</p>
            <input type="file" id="fileInput" multiple style="display: none;" accept=".flac,.wav,.mp3,.m4a,.aac,.dsf,.dff,.cue,.lrc">
            <button class="btn-upload" onclick="document.getElementById('fileInput').click()">Select Files</button>
        </div>

        <div class="file-list" id="uploadList" style="display: none;">
            <h4 style="margin-bottom: 12px;">Active Ingestions</h4>
            <div id="fileContainer"></div>
        </div>
    </div>

    <script>
        var dropzone = document.getElementById('dropzone');
        var fileInput = document.getElementById('fileInput');
        var uploadList = document.getElementById('uploadList');
        var fileContainer = document.getElementById('fileContainer');

        dropzone.addEventListener('dragover', function(e) { e.preventDefault(); dropzone.classList.add('dragover'); });
        dropzone.addEventListener('dragleave', function() { dropzone.classList.remove('dragover'); });
        dropzone.addEventListener('drop', function(e) {
            e.preventDefault();
            dropzone.classList.remove('dragover');
            handleFiles(e.dataTransfer.files);
        });

        fileInput.addEventListener('change', function(e) { handleFiles(e.target.files); });

        function fetchStats() {
            fetch('/api/status')
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    document.getElementById('storageFree').innerText = (data.storageFreeBytes / (1024*1024*1024)).toFixed(1) + ' GB';
                    document.getElementById('trackCount').innerText = data.totalTracks;
                })
                .catch(function() {});
        }
        fetchStats();

        function handleFiles(files) {
            if (!files.length) return;
            uploadList.style.display = 'block';

            for (var i = 0; i < files.length; i++) {
                (function(file) {
                    var safeId = file.name.replace(/[^a-zA-Z0-9]/g, '_');
                    var item = document.createElement('div');
                    item.className = 'file-item';
                    item.innerHTML = '<div class="file-info" style="width: 100%;">' +
                        '<div class="file-name">' + file.name + '</div>' +
                        '<div class="file-meta">' + (file.size / (1024*1024)).toFixed(2) + ' MB • Ingesting...</div>' +
                        '<div class="progress-bar"><div class="progress-fill" id="p_' + safeId + '"></div></div>' +
                        '</div>';
                    fileContainer.prepend(item);

                    var formData = new FormData();
                    formData.append('file', file);

                    var xhr = new XMLHttpRequest();
                    xhr.open('POST', '/api/upload', true);
                    xhr.upload.onprogress = function(e) {
                        if (e.lengthComputable) {
                            var pct = Math.round((e.loaded / e.total) * 100);
                            var bar = document.getElementById('p_' + safeId);
                            if (bar) bar.style.width = pct + '%';
                        }
                    };
                    xhr.onload = function() {
                        item.querySelector('.file-meta').innerText = 'Ingested into Library ✓';
                        item.querySelector('.file-meta').style.color = 'var(--accent-green)';
                        fetchStats();
                    };
                    xhr.send(formData);
                })(files[i]);
            }
        }
    </script>
</body>
</html>
    """.trimIndent()
}
