package com.csc108.model.cache;

import com.csc108.database.SqlSession;
import com.csc108.log.LogFactory;
import com.csc108.model.cache.AlgoCache;
import com.csc108.model.data.*;
import com.csc108.model.market.MicroStructure;
import com.csc108.utility.DateTimeUtil;
import com.csc108.utility.DateUtil;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by NIUXX on 2016/12/11.
 */
public class TradingSessionGroupCache extends AlgoCache<String,SessionGroup> {

   public void init() throws Exception {
       List<TradingSession> tradingSessions = new ArrayList<>();

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

               LocalTime dtStartTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:dd"));
               LocalTime dtEndTime = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm:dd"));

               String sessionTypeStr = ds.getString("sessionName");
               SessionType sessionType = SessionType.valueOf(sessionTypeStr);

               int auctionFlag = ds.getInt("auctionFlag");
               AuctionType auctionType = AuctionType.None;
               switch (auctionFlag){
                   case 1:
                       auctionType = AuctionType.AMAuction;
                       break;
                   case 2:
                       auctionType = AuctionType.PMAuction;
                       break;
                   case 3:
                       auctionType = AuctionType.CloseAuction;
                       break;
                   case 4:
                       auctionType = AuctionType.All;
                       break;
               }

               boolean isTradable = ds.getBoolean("isTradable");

               TradingSession tradingSession = new TradingSession("default",sessionGroup,sessionType,dtStartTime,dtEndTime,ops,auctionType,isTradable);
               tradingSessions.add(tradingSession);
           }

           Map<String,List<TradingSession>> tradingSessionByGroup= tradingSessions.stream().collect(Collectors.groupingBy(TradingSession::getSessionGroup));
           tradingSessionByGroup.keySet().forEach(k->{
               SessionGroup sessionGroup = new SessionGroup(k,tradingSessionByGroup.get(k));
               put(sessionGroup.name,sessionGroup);
           });

       }catch (Exception ex){
           LogFactory.error("initialize micorostructure failed!",ex);
           throw ex;
       }finally {
           if(session!=null)
               session.stop();
       }
   }
}
