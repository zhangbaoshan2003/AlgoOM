package com.csc108.monitor.command;

import com.csc108.model.cache.OrderbookDataManager;
import org.apache.commons.cli.CommandLine;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhangbaoshan on 2016/5/31.
 */
public class DataCommand  extends CommandBase {

    public DataCommand(){
        options.addOption("s",true,"security id to fake");
        options.addOption("d",true,"exchange for symbol to fake");
        options.addOption("p",true,"pre close price to fake");
        options.addOption("l",true,"last price to fake");
        options.addOption("o",true,"open price to fake");
        options.addOption("c",true,"open price to fake");
        options.addOption("a",true,"ask price to fake");
        options.addOption("b",true,"bid price to fake");
    }

    @Override
    public String getFirstLevelKey(){
        return "data";
    }

    @Override
    public String getSecondLevelKey(){
        return "orderbook";
    }

    public String list(String[] args){
        try {
            return OrderbookDataManager.getInstance().toString();
        }catch (Exception ex){
            return  "Error happened when list market data!"+ex;
        }
    }

    public String fake(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("s")==false){
                return "security id is not provided!";
            }
            if(cml.hasOption("d")==false){
                return "exchange destination is not provided!";
            }

            if(cml.hasOption("l")==false){
                return "last price is not provided!";
            }

            if(cml.hasOption("o")==false){
                return "open price is not provided!";
            }

            if(cml.hasOption("c")==false){
                return "close price is not provided!";
            }

            if(cml.hasOption("a")==false){
                return "ask price is not provided!";
            }

            if(cml.hasOption("b")==false){
                return "bid price is not provided!";
            }

            if(cml.getOptionValue("p")==null){
                return "pre close to be faked is not provided!";
            }

            String securityID= cml.getOptionValue("s");
            String dest = cml.getOptionValue("d");
            double preClose = Double.parseDouble(cml.getOptionValue("p"));
            double lastPrice= Double.parseDouble(cml.getOptionValue("l"));
            double openPrice= Double.parseDouble(cml.getOptionValue("o"));
            double closePrice= Double.parseDouble(cml.getOptionValue("c"));
            double askPrice= Double.parseDouble(cml.getOptionValue("a"));
            double bidPrice= Double.parseDouble(cml.getOptionValue("b"));

            if(dest=="sz"){
                OrderbookDataManager.getInstance().subscribeOrderBook(securityID + ".sz", false, null);
                OrderbookDataManager.getInstance().publish_SZ_SecurityData(securityID, preClose, lastPrice, openPrice,
                        closePrice, askPrice, bidPrice);
                TimeUnit.SECONDS.sleep(1);
                return OrderbookDataManager.getInstance().getLatestOrderBook(securityID + ".sz").toString();
            }else{
                OrderbookDataManager.getInstance().subscribeOrderBook(securityID+".sh",false,null);
                OrderbookDataManager.getInstance().publish_SH_SecurityData(securityID, preClose, lastPrice, openPrice,
                        closePrice, askPrice, bidPrice);
                TimeUnit.SECONDS.sleep(1);
                return OrderbookDataManager.getInstance().getLatestOrderBook(securityID+".sh").toString();
            }

        }catch (Exception ex){
            return "Fake order book error:"+ex.getStackTrace();
        }

    }

}
