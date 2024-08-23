package com.Orio.web_scraping_tool.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

public class ThreadUtil {

    public static int getNumAvailableThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

        long waitingThreadsCount = Arrays.stream(threadInfos)
                .filter(threadInfo -> threadInfo != null)
                .map(ThreadInfo::getThreadState)
                .filter(state -> state == Thread.State.WAITING || state == Thread.State.TIMED_WAITING)
                .count();

        return threadInfos.length - (int) waitingThreadsCount;
    }
}
