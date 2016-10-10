package com.csc108.model;

/**
 * Created by zhangbaoshan on 2016/5/6.
 */
public interface IDataHandler
{
    String getID();

    long getCreatedTimeStamp();

    void setCreatedTimeStamp(long createdTimeStamp);

    long getEnqueueTimeStamp();

    void setEnqueueTimeStamp(long enqueueTimeStamp) ;

    long getHandledTimeStamp() ;

    void setHandledTimeStamp(long handledTimeStamp) ;

    void setOriginalThread(long threadID);
    long getOriginalThreadId();

//    void setDisruptorId(long threadID);
//    long getDisruptorId();

}
