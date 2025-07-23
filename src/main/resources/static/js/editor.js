class UploadAdapter {
    constructor(loader) {
        this.loader = loader;
    }
    upload() {
        return new Promise((resolve, reject) => {
            const data = new FormData();
            this.loader.file.then(file => {
                data.append('uploadFile', file);
                $.ajax({
                    url: 'image/upload',
                    type: 'POST',
                    data: data,
                    dataType: 'json',
                    processData: false,
                    contentType: false,
                    success: function (data) {
                        if (data) {
                            resolve({ default: data.url });
                        } else {
                            reject(data.msg);
                        }
                    },
                    error: function(err) {
                        reject(err);
                    }
                });
            });
        });
    }
    abort() {}
}

var isEditorInitialized = false;
var editorInstance = null;

function initEditor(initialData = '') {
    if (isEditorInitialized && editorInstance) {
        editorInstance.setData(initialData);
        return Promise.resolve(editorInstance);
    }

    return CKEDITOR.ClassicEditor.create(document.getElementById("editor-note"), {
        toolbar: {
            items: [
                'exportPDF','exportWord', '|',
                'findAndReplace', '|',
                'heading', '|',
                'bold', 'italic', 'strikethrough', 'underline', 'removeFormat', '|',
                'bulletedList', 'numberedList', 'todoList', '|',
                'outdent', 'indent', '|',
                'undo', 'redo',
                '-',
                'fontSize', 'fontFamily', 'fontColor', 'highlight', '|',
                'alignment', '|',
                'link', 'uploadImage', 'blockQuote', 'insertTable', 'mediaEmbed', '|',
                'specialCharacters', 'horizontalLine'
            ],
            shouldNotGroupWhenFull: true
        },
        // 其他設定...（你原本的都放這裡）
        placeholder: '撰寫筆記內容',
        // ...省略其他設定 ...
        removePlugins: [
            'AIAssistant', 'CKBox', 'CKFinder', 'EasyImage',
            'MultiLevelList', 'RealTimeCollaborativeComments', 'RealTimeCollaborativeTrackChanges',
            'RealTimeCollaborativeRevisionHistory', 'PresenceList', 'Comments', 'TrackChanges',
            'TrackChangesData', 'RevisionHistory', 'Pagination', 'WProofreader', 'MathType',
            'SlashCommand', 'Template', 'DocumentOutline', 'FormatPainter', 'TableOfContents',
            'PasteFromOfficeEnhanced', 'CaseChange'
        ]
    }).then(editor => {
        editor.plugins.get('FileRepository').createUploadAdapter = (loader) => {
            return new UploadAdapter(loader);
        };

        editorInstance = editor;
        isEditorInitialized = true;

        editor.model.document.on('change:data', () => {
            const alpineComponent = document.body.__x?.$data;
            if (alpineComponent) {
                alpineComponent.unsavedLabel = true;
                debouncedSaveNote();
            }
        });
        window.editor = editor;
        editor.setData(initialData);

        return editor;
    }).catch(err => {
        console.error(err);
        throw err;
    });
}

function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}

const debouncedSaveNote = debounce(() => {
    const alpineComponent = document.body.__x?.$data;
    if (alpineComponent) {
        alpineComponent.saveNote();
    }
}, 1500);