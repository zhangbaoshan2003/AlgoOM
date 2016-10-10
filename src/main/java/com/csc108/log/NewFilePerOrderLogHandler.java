package com.csc108.log;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhangbaoshan on 2016/8/3.
 */
public class NewFilePerOrderLogHandler implements Runnable {
    private static AtomicInteger counter= new AtomicInteger(1);

    private final long MAX_SIZE_PER_FILE= 1024*1024;
    private String folder="./orders";
    private String back_up_folder="./orders";
    private String fileName;
    private Collection<String> lines;
    public NewFilePerOrderLogHandler(String clientOrderId,Collection<String> content){
        fileName = folder+"/"+ clientOrderId+".log";
        lines= content;
    }

    public void run(){
        try{
            File file = new File(fileName);
            if(file.exists()==false)
                file.createNewFile();

            if(file.length()>MAX_SIZE_PER_FILE){
                File desFile = new File( fileName + "_" + Integer.toString(counter.incrementAndGet()));
                FileUtils.copyFile(file,desFile);
                FileUtils.deleteQuietly(file);
                file.createNewFile();
            }

            FileUtils.writeLines(file,lines,true);

        }catch (Exception ex){
            LogFactory.error("process NewFilePerOrderLogHandler error",ex);
        }
    }
}
