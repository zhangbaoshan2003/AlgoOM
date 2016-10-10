package com.csc108.model.cache;

import com.csc108.database.SqlSession;
import com.csc108.log.LogFactory;
import com.csc108.model.market.MicroStructure;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class MicroStructureDataManager {
//    private final String fechCmdText = "  WITH  D1\n" +
//            "          AS ( SELECT   *\n" +
//            "               FROM     ( SELECT    ROW_NUMBER() OVER ( PARTITION BY stockId ORDER BY tradingDay DESC ) AS rn ,\n" +
//            "                                    *\n" +
//            "                          FROM      dbo.Facts21 F\n" +
//            "                        ) D\n" +
//            "               WHERE    D.rn = 1\n" +
//            "             ),\n" +
//            "        D2\n" +
//            "          AS ( SELECT   *\n" +
//            "               FROM     ( SELECT    ROW_NUMBER() OVER ( PARTITION BY stockId ORDER BY tradingDay DESC ) AS rn ,\n" +
//            "                                    *\n" +
//            "                          FROM      dbo.DailyFacts F\n" +
//            "                        ) D\n" +
//            "               WHERE    D.rn = 1\n" +
//            "             )\n" +
//            "    SELECT  D1.* ,\n" +
//            "            ISNULL(D2.closePrice, -1) AS ccp ,\n" +
//            "            ISNULL(D2.adjustedClose, -1) AS daccp\n" +
//            "    FROM    D1\n" +
//            "            LEFT JOIN D2 ON D1.stockId = D2.stockId";

    private final String fechCmdText = "  WITH  D1\n" +
            "          AS ( SELECT   *\n" +
            "               FROM     ( SELECT    ROW_NUMBER() OVER ( PARTITION BY stockId ORDER BY tradingDay DESC ) AS rn ,\n" +
            "                                    *\n" +
            "                          FROM      (SELECT  top 100 * FROM dbo.Facts21 WHERE exchange='sh' ) F\n" +
            "                        ) D\n" +
            "               WHERE    D.rn = 1\n" +
            "             ),\n" +
            "        D2\n" +
            "          AS ( SELECT   *\n" +
            "               FROM     ( SELECT    ROW_NUMBER() OVER ( PARTITION BY stockId ORDER BY tradingDay DESC ) AS rn ,\n" +
            "                                    *\n" +
            "                          FROM      (SELECT top 100 * FROM dbo.DailyFacts WHERE exchange='sh' ) F\n" +
            "                        ) D\n" +
            "               WHERE    D.rn = 1\n" +
            "             )\n" +
            "    SELECT  D1.* ,\n" +
            "            ISNULL(D2.closePrice, -1) AS ccp ,\n" +
            "            ISNULL(D2.adjustedClose, -1) AS daccp\n" +
            "    FROM    D1\n" +
            "            LEFT JOIN D2 ON D1.stockId = D2.stockId";

    private MicroStructureDataManager(){

    }

    private final HashMap<String, MicroStructure> microStructureHashMap = new HashMap<>();
    public HashMap<String, MicroStructure> getMicroStructureHashMap(){
        return microStructureHashMap;
    }

    private static MicroStructureDataManager _instance = new MicroStructureDataManager();

    public static MicroStructureDataManager getInstance() {
        return _instance;
    }

    public void initialize() throws Exception {
        SqlSession session =null;
        try{
            session = new SqlSession();
            ResultSet ds = session.dataSetQuery(fechCmdText);
            while (ds.next()){
                String mdSymbol=  ds.getString("stockId");
                double ats = ds.getLong("ats");
                double tradePeriod = ds.getDouble("tradePeriod");

                double quoteSize = ds.getLong("quoteSize");
                double turnoverPeriod = ds.getDouble("turnoverPeriod");
                double tickPeriod = ds.getDouble("tickPeriod");
                double spreadPeriod = ds.getDouble("spreadPeriod");
                double twSpread = ds.getDouble("twSpread");

                long adv20 = ds.getLong("adv20");
                long mdv21 = ds.getLong("mdv21");
                double ccp = ds.getDouble("ccp");
                double daccp = ds.getDouble("daccp");

                MicroStructure ms=new MicroStructure(mdSymbol,ats,tradePeriod,quoteSize,turnoverPeriod,
                        tickPeriod,spreadPeriod,twSpread,adv20,mdv21,ccp,daccp);

                microStructureHashMap.put(ms.getSymbol(),ms);
            }

        }catch (Exception ex){
            LogFactory.error("initialize micorostructure failed!",ex);
            throw ex;
        }finally {
            if(session!=null)
                session.stop();
        }
    }

    public MicroStructure getMicroStructure(String stokcId){
        if(microStructureHashMap.keySet().contains(stokcId))
            return microStructureHashMap.get(stokcId);

        return microStructureHashMap.get("default.sh");
    }
}
