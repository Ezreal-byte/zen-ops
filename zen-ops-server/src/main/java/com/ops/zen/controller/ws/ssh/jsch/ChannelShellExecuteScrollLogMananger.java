package com.ops.zen.controller.ws.ssh.jsch;

import com.google.common.base.Stopwatch;
import com.ops.zen.cache.Pair;
import com.ops.zen.controller.ws.ssh.fac.DefaultSshConnCfg;
import com.ops.zen.controller.ws.ssh.fac.PasswordSshConnCfg;
import com.ops.zen.controller.ws.ssh.fac.PrvKeySshConnCfg;
import com.ops.zen.controller.ws.ssh.fac.SshChannelTpEn;
import com.ops.zen.utils.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.ops.zen.utils.*;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * @author xyn
 * @date 2025/4/9 20:45
 * @description
 **/
public class ChannelShellExecuteScrollLogMananger {

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(ChannelShellExecuteScrollLogMananger.class);

    private BiFunction<String, Object, Object> logCompletePostFunc;

    private volatile static ChannelShellExecuteScrollLogMananger _inst;


    private ChannelShellExecuteScrollLogMananger() {

        // 清理长时间没有写入的日志
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                Iterator<Map.Entry<String, ExecuteResult>> iterator = resultMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ExecuteResult> next = iterator.next();
                    ExecuteResult value = next.getValue();
                    long lastModifiedMs = value.getFile().lastModified();
                    FileTime fileTime = Files.readAttributes(value.getFile().toPath(), BasicFileAttributes.class).creationTime();
                    long createTimeMs = fileTime.toMillis();
                    long elapsedFromModifiedMs = System.currentTimeMillis() - lastModifiedMs;
                    long elapsedFromCreateMs = System.currentTimeMillis() - createTimeMs;
                    if (elapsedFromModifiedMs > 1000 * 60 * 60) { // 日志60分钟未更新，视为已经结束
                        doClean(iterator, value);
                    } else if (elapsedFromCreateMs > 1000 * 60 * 120) { // 文件创建时间超过120分钟，执行时间过长，也清理掉
                        doClean(iterator, value);
                    }
                }
            } catch (Exception e) {
                logger.warn("", e);
            }
        }, 10, 60, TimeUnit.SECONDS);
    }

    private void doClean(Iterator<Map.Entry<String, ExecuteResult>> iterator, ExecuteResult value) {
        try {
            if (value.getChannel() != null) {
                value.getChannel().disconnect();
            }
        } catch (Exception e) {
            logger.warn("", e);
        }
        // TODO 删除文件？
        iterator.remove();
    }

    public static ChannelShellExecuteScrollLogMananger inst() {
        if (_inst == null) {
            synchronized (ChannelShellExecuteScrollLogMananger.class) {
                if (_inst != null) {
                    return _inst;
                }
                _inst = new ChannelShellExecuteScrollLogMananger();
            }
        }
        return _inst;
    }

    private AtomicInteger threadNum = new AtomicInteger();

    private Map<String, ExecuteResult> resultMap = new ConcurrentHashMap<>();

    public ExecuteResult closeAndRemoveResult(String id) {
        ExecuteResult executeResult = resultMap.remove(id);
        if (executeResult != null && executeResult.getChannel() != null) {
            try {
                executeResult.getChannel().disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return executeResult;
    }

    /**
     * 获取shell执行结果对象
     *
     * @param id
     * @return
     */
    public ExecuteResult get(String id) {
        return resultMap.get(id);
    }

    /**
     * @param sshConnCfg
     * @param cmds
     * @param extraInfo    执行完毕后调用logCompletePostFunc传递的第二个参数
     * @param autoExit     是否在cmds命令后追加exit命令，用于完成的标志（退出shell）使shell结束，输出结束
     * @param exitOnFaield
     * @return
     * @throws IOException
     */
    public String execute(DefaultSshConnCfg sshConnCfg, List<String> cmds, Object extraInfo, boolean autoExit, boolean exitOnFaield) throws IOException {
        if (autoExit) {
            cmds.add("exit");
        }
        if (exitOnFaield) {
            cmds.add(0, "set -e"); // 遇到异常退出shell
        }
        // cmds.add(1, "testa");
        sshConnCfg.setInitCmd(StringUtils.concate(cmds, "\n").concat("\n"));
        sshConnCfg.setTermTp(TermTypeEn.UN_SUPPORT);
        Channel channel = ChannelFactory.inst().createChannel(sshConnCfg);
        // 设置：col标识每行容纳多少列（字符）
        ((ChannelShell) channel).setPtySize(200, 600, 0, 0);

        File tempFile = FileUtils.createTempFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile)));

        String id = UUIDUtils.randomUUID();
        /*
           读取反馈，写入临时文件
           需要主动关闭channel，让线程结束
         */
        new Thread(() -> {
            try {
                InputStream inputStream = channel.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                /*int line;
                while ((line = reader.read()) != -1) {
                    bufferedWriter.write(line);
                    bufferedWriter.flush();
                }*/
                String line = null;
                while ((line = reader.readLine()) != null) {
                    bufferedWriter.write(line);
                    bufferedWriter.write("\n");
                    bufferedWriter.flush();
                }
                bufferedWriter.write("结束，请检查执行日志\n");
                bufferedWriter.write("线程结束，shell读取终端输出写入日志文件线程结束\n");
                bufferedWriter.flush();
                logger.info("线程结束，shell读取终端输出写入日志文件线程结束");
                if (logCompletePostFunc != null) {
                    logCompletePostFunc.apply(id, extraInfo);
                }
                // 日志结束标记
                ExecuteResult executeResult = get(id);
                if (executeResult != null) {
                    executeResult.setLogComplete(true);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    bufferedWriter.flush();
                } catch (Exception e) {
                }
                IOUtils.close(bufferedWriter);
            }
        }, "t-ChannelShellExecuteScrollLogMananger-" + threadNum.incrementAndGet()).start();

        // 发送命令
        OutputStream outputStream = channel.getOutputStream();
        outputStream.write(sshConnCfg.getInitCmd().getBytes());
        outputStream.flush();
        ExecuteResult rt = new ExecuteResult();
        rt.setChannel(channel);
        rt.setFile(tempFile);
        rt.setId(id);
        resultMap.put(id, rt);
        return id;
    }

    /**
     * 返回执行id
     *
     * @param isSeceretKey
     * @param host
     * @param sshPort
     * @param userName
     * @param password
     * @param seceretKey
     * @param cmds
     * @param exitOnFaield
     * @return
     * @throws IOException
     */
    public String execute(boolean isSeceretKey, String host, int sshPort, String userName, String password, String seceretKey, List<String> cmds, boolean exitOnFaield) throws IOException {
        DefaultSshConnCfg key = null;
        if (isSeceretKey) {
            key = new PrvKeySshConnCfg(SshChannelTpEn.SHELL, host, sshPort, userName, seceretKey, password, "");
        } else {
            key = new PasswordSshConnCfg(SshChannelTpEn.SHELL, host, sshPort, userName, password, "");
        }
        return execute(key, cmds, null, false, exitOnFaield);
    }

    /**
     * outLog的onceContent为null表示读取完毕
     *
     * @param id
     * @param outLog
     * @return
     */
    public OutLog output(String id, OutLog outLog) {
        outLog.setOnceContent(null);
        File logFile = resultMap.get(id).getFile();
        // 读取临时文件中的输出
        int charStartPos = outLog.getCharStartPos();
        int lineLimitNum = outLog.getLineLimitNum();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile)));
            reader.skip(charStartPos);
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            if (line == null) {
                return outLog;
            }
            sb.append(line);
            int times = 0;
            while (++times <= lineLimitNum && line != null) {
                sb.append("\n");
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
            }
            charStartPos += sb.length();

            outLog.setCharStartPos(charStartPos);
            outLog.setOnceContent(sb.length() == 0 ? null : sb.toString());
            return outLog;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void setLogCompletePostFunc(BiFunction<String, Object, Object> postFunc) {
        this.logCompletePostFunc = postFunc;
    }

    /**
     * 同步获取执行脚本的日志，获取的日志时间范围-maxLastTimeMs
     *
     * @param id
     * @param maxLastTimeMs
     * @return
     */
    public StringBuilder syncLog(String id, Long maxLastTimeMs) {
        StringBuilder sb = new StringBuilder();
        OutLog inPOut = new OutLog(); // 入参outLog
        inPOut.setLineLimitNum(10);
        inPOut.setCharStartPos(0);
        OutLog out = output(id, inPOut);
        if (StringUtils.isNotEmpty(out.getOnceContent())) {
            sb.append(out.getOnceContent());
            sb.append("\n");
        }
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        Long lastTimeMs = 0l;
        while (true) {
            stopwatch.start();
            out = output(id, out);
            if (out.getOnceContent() == null) {
                ThreadUtils.sleepWithoutEx(10l);
            } else {
                sb.append(out.getOnceContent());
                sb.append("\n");
            }
            lastTimeMs += stopwatch.elapsed(TimeUnit.MILLISECONDS);
            stopwatch.reset();
            if (lastTimeMs >= maxLastTimeMs) {
                break;
            }
        }
        return sb;
    }

    /**
     * 执行结果对象
     */
    public static class ExecuteResult {

        private File file;

        private String id;

        private Channel channel;

        private boolean logComplete;

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Channel getChannel() {
            return channel;
        }

        public void setChannel(Channel channel) {
            this.channel = channel;
        }

        public boolean isLogComplete() {
            return logComplete;
        }

        public void setLogComplete(boolean logComplete) {
            this.logComplete = logComplete;
        }
    }

    /**
     * 输出日志对象
     */
    public static class OutLog {

        private int charStartPos;

        private int lineLimitNum;

        private String onceContent;

        public int getCharStartPos() {
            return charStartPos;
        }

        public void setCharStartPos(int charStartPos) {
            this.charStartPos = charStartPos;
        }

        public int getLineLimitNum() {
            return lineLimitNum;
        }

        public void setLineLimitNum(int lineLimitNum) {
            this.lineLimitNum = lineLimitNum;
        }

        public String getOnceContent() {
            return onceContent;
        }

        public void setOnceContent(String onceContent) {
            this.onceContent = onceContent;
        }
    }


    /**
     * 前端通过key获取滚动日志
     *
     * @return key：logId，value：滚动日志写入的writer。
     * @throws IOException
     */
    public Pair<String, BufferedWriter> scrollLogPair() throws IOException {
        File tempFile = FileUtils.createTempFile();
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile)));
        String id = UUIDUtils.randomUUID();
        ExecuteResult rt = new ExecuteResult();
        rt.setFile(tempFile);
        rt.setId(id);
        resultMap.put(id, rt);
        return new Pair<>(id, bufferedWriter);
    }

}
