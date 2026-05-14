import { fileURLToPath, URL } from 'node:url'
import { loadEnv, ConfigEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite' // 自动导入
import Components from 'unplugin-vue-components/vite' // 组件注册
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers' // elementPlus
import Icons from 'unplugin-icons/vite' // icon相关
import IconsResolver from 'unplugin-icons/resolver' // icon相关

export default ({ mode }: ConfigEnv) => {
  const { VITE_PUBLIC_PATH, VITE_PROXY_DOMAIN, VITE_PROXY_DOMAIN_REAL } = loadEnv(
    mode,
    process.cwd()
  )
  return {
    base: VITE_PUBLIC_PATH, //打包路径
    plugins: [
      vue(),
      AutoImport({
        imports: ['vue', 'vue-router'],
        dts: fileURLToPath(new URL('./types/auto-imports.d.ts', import.meta.url)),
        resolvers: [
          ElementPlusResolver(),
          // 自动导入图标组件
          IconsResolver({
            prefix: 'Icon'
          })
        ]
      }),
      Components({
        dirs: ['src/views', 'src/layout', 'src/components'],
        dts: fileURLToPath(new URL('./types/components.d.ts', import.meta.url)),
        resolvers: [
          ElementPlusResolver(),
          IconsResolver({
            enabledCollections: ['ep'] // 重点
          })
        ]
      }),
      Icons({
        autoInstall: true
      })
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    // 启动服务配置
    server: {
      host: '0.0.0.0',
      port: 8000,
      open: false,
      https: false,
      proxy: {
        '/platform': {
          target: 'http://localhost:9998',
          changeOrigin: true,
          ws: true,
          rewrite: (path: string) => path
        }
      }
    },
    // 生产环境打包配置
    build: {
      minify: 'terser',
      terserOptions: {
        compress: {
          // 保留 xterm.js 需要的变量
          keep_fnames: /xterm/,
          passes: 1
        },
        mangle: {
          // 不混淆 xterm 相关变量
          reserved: ['Terminal', 'FitAddon', 'SearchAddon', 'WebLinksAddon']
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'js/[name]-[hash].js',
          entryFileNames: 'js/[name]-[hash].js',
          manualChunks(id: string | string[]) {
            if (id.includes('node_modules')) {
              const arr = id.toString().split('node_modules/')
              return arr[arr.length - 1].split('/')[0].toString()
            }
          }
        }
      }
    },
    optimizeDeps: {
      exclude: ['monaco-editor/esm/nls.messages.zh-cn.js']
    }
  }
}
