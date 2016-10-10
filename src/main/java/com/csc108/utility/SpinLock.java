package com.csc108.utility;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by zhangbaoshan on 2016/5/31.
 */
public class SpinLock {
    private final AtomicReference<Thread> _lock = new AtomicReference<>(null);
    private final Lock _unLock = new Lock();
    public class Lock implements AutoCloseable{

        @Override
        public void close() throws Exception {

        }
    }

    public Lock lock(){
        Thread thread = Thread.currentThread();
        while (true){
            if(!_lock.compareAndSet(null,thread)){
                if(_lock.get()==thread)
                    throw new IllegalStateException("SpinLock dosen't support reentrant");
                continue;
            }
            return _unLock;
        }

    }

    public boolean isLocked(){
        return _lock.get()!=null;
    }

    public boolean isLockedThread(){
        return _lock.get()==Thread.currentThread();
    }
}
