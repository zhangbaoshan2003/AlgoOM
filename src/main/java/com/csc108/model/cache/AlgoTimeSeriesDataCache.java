package com.csc108.model.cache;

import com.csc108.exceptions.TimeOutException;
import com.csc108.log.LogFactory;
import com.csc108.model.ITimeable;
import com.csc108.utility.DateTimeUtil;
import com.google.common.collect.Lists;
import org.antlr.stringtemplate.language.ArrayWrappedInList;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by zhangbaoshan on 2016/9/6.
 */
public class AlgoTimeSeriesDataCache<T extends ITimeable> extends AlgoCache<String,ArrayList<T>> {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Object mainLock = new Object();
    private final long defaultLockTimeOut=5000;

    public void put(String key, T t){
        boolean lockAvailable=false;
        try {
            ArrayList<T> datas = super.get(key);
            if(datas==null){
                synchronized (mainLock){
                    if(datas==null){
                        datas = Lists.<T>newArrayList();
                        put(key,datas);
                    }
                }
            }

            lockAvailable = readWriteLock.writeLock().tryLock(defaultLockTimeOut, TimeUnit.MILLISECONDS);
            if(lockAvailable!=true){
                throw new TimeOutException(String.format
                        ("Unable to acquire write lock after %s millseconds ",defaultLockTimeOut));
            }

            if(datas.size()==0){
                datas.add(t);
                return;
            }

            T last = datas.get(datas.size()-1);
            datas.add(t);

            if((t.getDateTime().isAfter(last.getDateTime()))|| (t.getDateTime().isEqual(last.getDateTime()))){

            }else{
                Collections.sort(datas,ITimeable.TimeComparator);
            }
        }catch (Exception ex){
            LogFactory.error("Update AlgoTimeSeriesDataCache error!",ex);
        }finally {
            if(lockAvailable==true)
                readWriteLock.writeLock().unlock();
        }
    }

    @Override
    public ArrayList<T> get(String key){
        ArrayList<T> list = new ArrayList<>();
        ArrayList<T> cachedList = super.get(key);
        if(cachedList!=null){
            list.addAll(cachedList);
        }
        return list;
    }

    public String outputString(String key){
        StringBuilder sb = new StringBuilder();
        ArrayList<T> timeSeries = get(key);
        if(timeSeries!=null){
            timeSeries.forEach(dataPoint->{
                sb.append(String.format("%s=%s,",
                        dataPoint.getDateTime().format(DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss")),
                        Long.toString(dataPoint.getQty())+".0"));
            });
        }
        sb.append("#");
        return sb.toString().replace(",#","");
    }

}
