package com.csc108.model.cache;

import com.csc108.database.SqlSession;
import com.csc108.log.LogFactory;
import com.csc108.model.cache.AlgoCache;
import com.csc108.model.data.AuctionType;
import com.csc108.model.data.Operation;
import com.csc108.model.data.SessionGroup;
import com.csc108.model.data.TradingSession;
import com.csc108.model.market.MicroStructure;
import com.csc108.utility.DateTimeUtil;
import com.csc108.utility.DateUtil;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NIUXX on 2016/12/11.
 */
public class TradingSessionGroupCache extends AlgoCache<String,SessionGroup> {

   public void Init() throws Exception {
       SqlSession session =null;
       try{
           session = new SqlSession();
           ResultSet ds = session.dataSetQuery("spu_GetTradingSession");
           while (ds.next()){
               String date=  ds.getString("date");
               String sessionGroup = ds.getString("sessionGroup");
               String sessionName = ds.getString("sessionName");
               String startTime = ds.getString("startTime");
               String endTime = ds.getString("endTime");
               String operations = ds.getString("operations");

               List<Operation> ops = new ArrayList<>();

               String[] opers = operations.split(",");
               for (int i=0;i<opers.length;i++){
                   Operation operation = Operation.valueOf(opers[i]);
                   ops.add(operation);
               }

               LocalDateTime dtStartTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:dd"));
               LocalDateTime dtEndTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm:dd"));

               int auctionFlag = ds.getInt("auctionFlag");
               AuctionType auctionType = new ;

               TradingSession tradingSession = new TradingSession("default",sessionGroup,dtStartTime,dtEndTime,ops,)
           }

       }catch (Exception ex){
           LogFactory.error("initialize micorostructure failed!",ex);
           throw ex;
       }finally {
           if(session!=null)
               session.stop();
       }
   }
}
