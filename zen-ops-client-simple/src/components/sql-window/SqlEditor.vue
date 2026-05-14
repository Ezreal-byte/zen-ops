<script setup lang="ts">
import {ref, watch, onMounted} from 'vue'
// 加载 Monaco Editor 中文语言包（必须在 monaco-editor 导入之前）
import 'monaco-editor/esm/nls.messages.zh-cn.js'
import * as monaco from 'monaco-editor'

const props = defineProps<{ modelValue: string }>()
const emit = defineEmits(['update:modelValue'])

const editorRef = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null

onMounted(() => {
  if (!editorRef.value) return
  editor = monaco.editor.create(editorRef.value, {
    value: props.modelValue,
    language: 'sql',
    theme: 'vs',
    automaticLayout: true,
    minimap: { enabled: false },
    fontSize: 13,
    lineNumbers: 'on',
    lineNumbersMinChars: 3,
    lineDecorationsWidth: 8,
    roundedSelection: false,
    scrollBeyondLastLine: false,
    readOnly: false,
    wordWrap: 'on',
  })
  editor.onDidChangeModelContent(() => {
    emit('update:modelValue', editor?.getValue() || '')
  })
})

watch(() => props.modelValue, (val) => {
  if (editor && editor.getValue() !== val) {
    editor.setValue(val)
  }
})

const getSelectedSql = (): string => {
  if (!editor) return ''
  const selection = editor.getSelection()
  if (selection && !selection.isEmpty()) {
    return editor.getModel()?.getValueInRange(selection) || ''
  }
  return ''
}

defineExpose({ getSelectedSql })
</script>

<template>
  <div ref="editorRef" class="sql-editor"></div>
</template>

<style scoped lang="less">
.sql-editor {
  width: 100%;
  height: 100%;

  :deep(.monaco-editor .margin) {
    background-color: #f5f7fa !important;
  }

  :deep(.monaco-editor .line-numbers) {
    color: #909399 !important;
  }
}
</style>
