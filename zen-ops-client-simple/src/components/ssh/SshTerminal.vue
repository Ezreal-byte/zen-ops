<template>
  <div class="ssh-terminal-wrapper" :class="{ 'light-theme': props.lightTheme }">
    <div class="terminal-body" ref="terminalRef"></div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { Terminal } from '@xterm/xterm'
import { FitAddon } from '@xterm/addon-fit'
import { WebLinksAddon } from '@xterm/addon-web-links'
import '@xterm/xterm/css/xterm.css'

const HEARTBEAT_INTERVAL = 30 * 1000
const HEARTBEAT_CONTENT = '________heart-resp________'
const CONNECTED_SIGNAL = 'websshshellconnected'

const props = defineProps<{
  serverId: string
  serverName: string
  initPath: string
  fontSize?: number
  lightTheme?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'reconnect'): void
  (e: 'disconnect'): void
}>()

const terminalRef = ref<HTMLElement>()
let term: Terminal | null = null
let fitAddon: FitAddon | null = null
let ws: WebSocket | null = null
let heartbeatTimer: ReturnType<typeof setInterval> | null = null
let isConnected = false

const getWsUrl = () => {
  const loc = window.location
  const protocol = loc.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${protocol}//${loc.host}/platform/ws/ssh`
}

const darkTheme = {
  background: '#1e1e1e',
  foreground: '#d4d4d4',
  cursor: '#ffffff',
  selectionBackground: '#264f78',
  black: '#000000',
  red: '#cd3131',
  green: '#0dbc79',
  yellow: '#e5e510',
  blue: '#2472c8',
  magenta: '#bc3fbc',
  cyan: '#11a8cd',
  white: '#e5e5e5',
  brightBlack: '#666666',
  brightRed: '#f14c4c',
  brightGreen: '#23d18b',
  brightYellow: '#f5f543',
  brightBlue: '#3b8eea',
  brightMagenta: '#d670d6',
  brightCyan: '#29b8db',
  brightWhite: '#ffffff'
}

const lightTheme = {
  background: '#ffffff',
  foreground: '#333333',
  cursor: '#333333',
  selectionBackground: '#add6ff',
  black: '#000000',
  red: '#cd3131',
  green: '#008000',
  yellow: '#949800',
  blue: '#0451a5',
  magenta: '#bc3fbc',
  cyan: '#0598bc',
  white: '#555555',
  brightBlack: '#666666',
  brightRed: '#cd3131',
  brightGreen: '#008000',
  brightYellow: '#949800',
  brightBlue: '#0451a5',
  brightMagenta: '#bc3fbc',
  brightCyan: '#0598bc',
  brightWhite: '#333333'
}

const initTerminal = () => {
  if (!terminalRef.value) return

  term = new Terminal({
    cursorBlink: true,
    cursorStyle: 'bar',
    fontSize: props.fontSize || 14,
    fontFamily: '"Cascadia Code", "Fira Code", "JetBrains Mono", Menlo, Monaco, Consolas, monospace',
    theme: props.lightTheme ? lightTheme : darkTheme,
    allowProposedApi: true
  })

  fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.loadAddon(new WebLinksAddon())

  term.open(terminalRef.value)
  nextTick(() => {
    fitAddon?.fit()
  })

  // 终端输入 -> WebSocket
  term.onData((data: string) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      // 发送文本消息（cmd操作）
      const cmd = JSON.stringify({ op: 'cmd', content: data })
      ws.send(cmd)
    }
  })

  // 监听resize事件
  term.onResize(({ cols, rows }) => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      const resize = JSON.stringify({ op: 'resize', content: JSON.stringify({ cols, rows }) })
      ws.send(resize)
    }
  })
}

const connectWebSocket = () => {
  const url = getWsUrl()
  ws = new WebSocket(url)
  ws.binaryType = 'arraybuffer'

  ws.onopen = () => {
    // 发送连接命令
    const connectContent = JSON.stringify({
      type: 'ID',
      channelType: 'shell',
      id: props.serverId,
      idType: 'SERVER',
      initPath: props.initPath || ''
    })
    const cmd = JSON.stringify({ op: 'connect', content: connectContent })
    ws!.send(cmd)
  }

  ws.onmessage = (event: MessageEvent) => {
    if (event.data instanceof ArrayBuffer) {
      const text = new TextDecoder('utf-8').decode(event.data)
      // 跳过心跳响应
      if (text === HEARTBEAT_CONTENT) return
      // 检测连接成功信号
      if (!isConnected && text.includes(CONNECTED_SIGNAL)) {
        isConnected = true
        // 发送初始resize
        if (term && fitAddon) {
          const resize = JSON.stringify({
            op: 'resize',
            content: JSON.stringify({ cols: term.cols, rows: term.rows })
          })
          ws!.send(resize)
        }
        // 过滤掉连接信号文本
        const filtered = text.replace(CONNECTED_SIGNAL, '')
        if (filtered && term) term.write(filtered)
        return
      }
      if (term) term.write(text)
    } else if (typeof event.data === 'string') {
      if (event.data === HEARTBEAT_CONTENT) return
      if (!isConnected && event.data.includes(CONNECTED_SIGNAL)) {
        isConnected = true
        if (term && fitAddon) {
          const resize = JSON.stringify({
            op: 'resize',
            content: JSON.stringify({ cols: term.cols, rows: term.rows })
          })
          ws!.send(resize)
        }
        const filtered = event.data.replace(CONNECTED_SIGNAL, '')
        if (filtered && term) term.write(filtered)
        return
      }
      if (term) term.write(event.data)
    }
  }

  ws.onclose = () => {
    if (term) {
      term.write('\r\n\x1b[31m--- 连接已关闭 ---\x1b[0m\r\n')
    }
    stopHeartbeat()
  }

  ws.onerror = () => {
    if (term) {
      term.write('\r\n\x1b[31m--- 连接错误 ---\x1b[0m\r\n')
    }
    stopHeartbeat()
  }

  // 启动心跳
  startHeartbeat()
}

const startHeartbeat = () => {
  stopHeartbeat()
  heartbeatTimer = setInterval(() => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify({ op: 'heartbeat', content: '' }))
    }
  }, HEARTBEAT_INTERVAL)
}

const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

const fit = () => {
  if (fitAddon && term) {
    try {
      fitAddon.fit()
    } catch (e) { /* ignore */ }
  }
}

// 动态调整字体大小
const setFontSize = (size: number) => {
  if (term) {
    term.options.fontSize = size
    nextTick(() => {
      try { fitAddon?.fit() } catch (e) { /* ignore */ }
    })
  }
}

defineExpose({ fit, setFontSize })

// 监听 fontSize prop 变化
watch(() => props.fontSize, (val) => {
  if (val) setFontSize(val)
})

// 监听 lightTheme prop 变化
watch(() => props.lightTheme, (val) => {
  if (term) {
    term.options.theme = val ? lightTheme : darkTheme
  }
})

// 监听窗口大小变化
let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  initTerminal()
  connectWebSocket()

  // 监听容器大小变化自动fit
  if (terminalRef.value) {
    resizeObserver = new ResizeObserver(() => {
      if (fitAddon && term) {
        try {
          fitAddon.fit()
        } catch (e) { /* ignore */ }
      }
    })
    resizeObserver.observe(terminalRef.value)
  }
})

onBeforeUnmount(() => {
  stopHeartbeat()
  if (resizeObserver) {
    resizeObserver.disconnect()
    resizeObserver = null
  }
  if (ws) {
    ws.close()
    ws = null
  }
  if (term) {
    term.dispose()
    term = null
  }
  isConnected = false
})
</script>

<style lang="less" scoped>
.ssh-terminal-wrapper {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #1e1e1e;

  &.light-theme {
    background: #ffffff;
  }
}

.terminal-body {
  flex: 1;
  padding: 4px;
  overflow: hidden;
  background: transparent;

  :deep(.xterm) {
    height: 100%;
  }

  :deep(.xterm-viewport) {
    overflow-y: auto !important;
  }
}
</style>
