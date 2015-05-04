package com.googlecode.greysanatomy.server;

import com.googlecode.greysanatomy.probe.ProbeJobs;
import com.googlecode.greysanatomy.util.IOUtils;
import com.googlecode.greysanatomy.util.LogUtils;

import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;

/**
 * Greys ����˻Ự
 * Created by vlinux on 15/5/2.
 */
public class GaSession {


    private final Logger logger = LogUtils.getLogger();

    // Java����ID
    private final int javaPid;

    // �ỰID
    private final int sessionId;

    // �Ự����ʱ��(��λ����)
    private final long sessionDuration;

    // �Ự��Ӧ��SocketChannel
    private final SocketChannel socketChannel;

    // �Ự�ַ���
    private Charset charset;

    // �Ự��������JobId��һ���Ựֻ�ܹ���һ��JobId
    private Integer currentJobId;

    // �Ự���һ�ν���ʱ��(����ʱ��)
    private volatile long gmtLastTouch;


    // �Ự�����ٱ�ǣ�һ���Ự�����ٽ��޷����»ָ�
    private final AtomicBoolean isDestroyRef = new AtomicBoolean(false);

    // �Ự��д��ǣ�д�򿪵ĻỰ�������������currentJobId����Ӧ������
    private volatile boolean jobRunning = false;

    public GaSession(int javaPid, int sessionId, long sessionDuration, SocketChannel socketChannel, Charset charset) {
        this.javaPid = javaPid;
        this.sessionId = sessionId;
        this.sessionDuration = sessionDuration;
        this.socketChannel = socketChannel;
        this.charset = charset;
        this.gmtLastTouch = currentTimeMillis();
    }

    /**
     * ���ٻỰ
     */
    public void destroy() {
        if (!isDestroyRef.compareAndSet(false, true)) {
            throw new IllegalStateException(format("session[%d] already destroyed.", sessionId));
        }

        if (null != currentJobId) {
            ProbeJobs.killJob(currentJobId);
        }

        IOUtils.close(socketChannel);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, format("session[%d] destroyed, currentJobId=%s", sessionId, currentJobId));
        }

    }

    /**
     * �����Ự<br/>
     * ��ʱ�����ĻỰ�п��ܻᱻ��ʱ
     */
    public void touch() {
        gmtLastTouch = currentTimeMillis();
    }

    /**
     * �Ự�Ƿ���
     *
     * @return true:�Ự����;false:�Ự��δ����
     */
    public boolean isExpired() {
        return sessionDuration <= currentTimeMillis() - gmtLastTouch;
    }

    @Override
    public String toString() {
        return format("GaSession[%d]:isExpired=%s;currentJobId=%s;", sessionId, isExpired(), currentJobId);
    }

    public void setCurrentJobId(Integer currentJobId) {
        this.currentJobId = currentJobId;
    }

    /**
     * ����Ƿ�������������
     *
     * @param isRunning true:������������;false:û������������
     */
    public void markJobRunning(boolean isRunning) {
        this.jobRunning = isRunning;
    }

    public int getSessionId() {
        return sessionId;
    }

    public Charset getCharset() {
        return charset;
    }

    public Integer getCurrentJobId() {
        return currentJobId;
    }

    /**
     * ��ǰ�Ƿ���������������
     *
     * @return
     */
    public boolean hasJobRunning() {
        return jobRunning;
    }

    public boolean isDestroy() {
        return isDestroyRef.get();
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public long getSessionDuration() {
        return sessionDuration;
    }

    public int getJavaPid() {
        return javaPid;
    }
}