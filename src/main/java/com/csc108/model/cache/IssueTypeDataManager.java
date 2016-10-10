package com.csc108.model.cache;

import com.csc108.database.SqlSession;
import com.csc108.log.LogFactory;
import com.csc108.model.market.IssueType;

import java.sql.ResultSet;
import java.util.HashMap;

/**
 * Created by zhangbaoshan on 2016/8/30.
 */
public class IssueTypeDataManager  {
    private final String fechCmdText = "SELECT  [exchange]\n" +
            "      ,[symbol]\n" +
            "      ,[stockId]\n" +
            "      ,[sType]\n" +
            "      ,[listDate]\n" +
            "      ,[delistDate]\n" +
            "      ,[status]\n" +
            "      ,[isTradable]\n" +
            "      ,[range]\n" +
            "      ,[noTradeDate]\n" +
            "      ,[reTradeDate]\n" +
            "      ,[stockName]\n" +
            "      ,[currency]\n" +
            "  FROM [DataService2].[dbo].[IssueType]";

    private HashMap<String,IssueType> _issueTypeHashMap=new HashMap<>();
    public HashMap<String,IssueType> IssueTypeHashMap(){
        return _issueTypeHashMap;
    }

    private IssueTypeDataManager(){

    }

    private static IssueTypeDataManager instance = new IssueTypeDataManager();


    public static IssueTypeDataManager getInstance() {
        return instance;
    }

    public void initialize() throws Exception {
        SqlSession session =null;
        try{
            session = new SqlSession();
            ResultSet ds = session.dataSetQuery(fechCmdText);
            while (ds.next()){
                String mdSymbol=  ds.getString("stockId");
                String exchange = ds.getString("exchange");
                String stockId = ds.getString("stockId");
                String sType =  ds.getString("sType");
                boolean status =  ds.getBoolean("status");
                boolean isTradable =  ds.getBoolean("isTradable");
                String stockName = ds.getString("stockName");

                IssueType issueType = new IssueType(exchange,mdSymbol,stockId,sType,
                        isTradable,status,stockName);

                _issueTypeHashMap.put(stockId,issueType);
            }
        }catch (Exception ex){
            LogFactory.error("initialize issue type failed!", ex);
            throw ex;
        }finally {
            if(session!=null)
                session.stop();
        }
    }
}
