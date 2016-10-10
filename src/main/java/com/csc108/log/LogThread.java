package com.csc108.log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by zhangbaoshan on 2016/8/3.
 */
public class LogThread extends Thread {
    private final BlockingQueue<Runnable> logTaskQueue = new LinkedBlockingQueue<>();

    public LogThread(){
        this.setDaemon(true);
    }

    @Override
    public void run(){
        while (!isInterrupted()){
            Runnable task=null;
            try {
                task = logTaskQueue.take();
                if(task!=null)
                    task.run();
            }catch (Exception ex){
                LogFactory.error("Logging error!",ex);
            }
        }
    }

    public void enqueueTask(Runnable logTask){
        logTaskQueue.add(logTask);
    }
}
