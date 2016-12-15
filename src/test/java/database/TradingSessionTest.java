package database;

import com.csc108.model.cache.TradingSessionGroupCache;
import com.csc108.model.data.AuctionType;
import com.csc108.model.data.IntervalType;
import com.csc108.model.data.SessionGroup;
import com.csc108.model.data.TradingSession;
import junit.framework.TestCase;

import java.time.LocalTime;

/**
 * Created by zhangbaoshan on 2016/12/12.
 */
public class TradingSessionTest extends TestCase {
    TradingSessionGroupCache tradingSessionGroupCache = new TradingSessionGroupCache();

    protected void setUp() throws Exception {
        tradingSessionGroupCache.init();
    }

    public void testTradingTime() throws Exception {
        String symbol1 = "600000.sh";

        String sessionGroupName= TradingSession.symbol2SessionGroup(symbol1);
        SessionGroup sessionGroupSH = tradingSessionGroupCache.get(sessionGroupName);

        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(7, 30, 30)));
        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(15, 30, 30)));
        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(12, 30, 30)));
        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(9, 26, 30)));
        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(9,20,0), IntervalType.End,AuctionType.None));
        assertEquals(true,sessionGroupSH.isTradable(LocalTime.of(9,20,0), IntervalType.End,AuctionType.AMAuction));
        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(9,20,0), IntervalType.End,AuctionType.PMAuction));
        assertEquals(true,sessionGroupSH.isTradable(LocalTime.of(9,20,0), IntervalType.Start,AuctionType.AMAuction));
        assertEquals(true,sessionGroupSH.isTradable(LocalTime.of(9,30,1)));

        assertEquals(true,sessionGroupSH.isTradable(LocalTime.of(15,0,0)));
        assertEquals(true,sessionGroupSH.isTradable(LocalTime.of(11,30,0)));

        assertEquals(false,sessionGroupSH.isTradable(LocalTime.of(13,00,0)));

        String symbol2 = "000596.sz";
        sessionGroupName= TradingSession.symbol2SessionGroup(symbol1);
        SessionGroup sessionGroupSZ = tradingSessionGroupCache.get(sessionGroupName);

        assertEquals(false,sessionGroupSZ.isTradable(LocalTime.of(7, 30, 30)));
        assertEquals(false,sessionGroupSZ.isTradable(LocalTime.of(15, 30, 30)));
        assertEquals(false,sessionGroupSZ.isTradable(LocalTime.of(12, 30, 30)));
        assertEquals(false,sessionGroupSZ.isTradable(LocalTime.of(9, 26, 30)));
        assertEquals(false,sessionGroupSZ.isTradable(LocalTime.of(9,20,0), IntervalType.End,AuctionType.None));
        assertEquals(true,sessionGroupSZ.isTradable(LocalTime.of(9,20,0), IntervalType.End,AuctionType.AMAuction));
        assertEquals(false,sessionGroupSZ.isTradable(LocalTime.of(9,20,0), IntervalType.End,AuctionType.PMAuction));
        assertEquals(true,sessionGroupSZ.isTradable(LocalTime.of(9,20,0), IntervalType.Start,AuctionType.AMAuction));
        assertEquals(true,sessionGroupSZ.isTradable(LocalTime.of(9,30,1)));

    }

    public void testPMAucationTime(){
        String symbol1 = "600000.sh";
        String symbol2 = "000596.sz";
        String sessionGroupName= TradingSession.symbol2SessionGroup(symbol1);
        SessionGroup sessionGroupSH = tradingSessionGroupCache.get(sessionGroupName);
        assertEquals(false, sessionGroupSH.isPMAuctionSession(LocalTime.of(12, 0, 0)));
        assertEquals(false, sessionGroupSH.isPMAuctionSession(LocalTime.of(8, 0, 0)));
        assertEquals(false, sessionGroupSH.isPMAuctionSession(LocalTime.of(14, 25, 0)));

        sessionGroupName= TradingSession.symbol2SessionGroup(symbol2);
        SessionGroup sessionGroupSZ = tradingSessionGroupCache.get(sessionGroupName);
        assertEquals(false, sessionGroupSZ.isPMAuctionSession(LocalTime.of(15, 0, 0)));
        assertEquals(false, sessionGroupSZ.isPMAuctionSession(LocalTime.of(14, 27, 0)));
    }

}
