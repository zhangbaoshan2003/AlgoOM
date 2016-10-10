package com.csc108.model.market;

/**
 * Created by LEGEN on 2016/8/28.
 */
public class MicroStructure {
    private String symbol;
    private double ats;
    private double tradePeriod;
    private double quoteSize;
    private double turnoverPeriod;
    private double spreadPeriod;
    private double tickPeriod;
    private double twSpread;
    private long adv20;
    private long mdv21;
    private double ccp;
    private double daccp;

    public MicroStructure(String mdSymbol_, double ats_, double  tradePeriod_, double  quoteSize_, double  turnoverPeriod_,
                          double  tickPeriod_, double  spreadPeriod_, double  twSpread_, long  adv20_, long mdv21_, double  ccp_, double  daccp_){
        this.symbol = mdSymbol_;
        this.ats = ats_;
        this.tradePeriod = tradePeriod_;
        quoteSize = quoteSize_;
        turnoverPeriod = turnoverPeriod_;
        tickPeriod = tickPeriod_;
        spreadPeriod = spreadPeriod_;
        twSpread = twSpread_;
        adv20 = adv20_;
        mdv21 = mdv21_;
        ccp = ccp_;
        daccp = daccp_;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getAts() {
        return ats;
    }

    public double getTradePeriod() {
        return tradePeriod;
    }

    public double getQuoteSize() {
        return quoteSize;
    }

    public double getTurnoverPeriod() {
        return turnoverPeriod;
    }

    public double getSpreadPeriod() {
        return spreadPeriod;
    }

    public double getTwSpread() {
        return twSpread;
    }

    public long getAdv20() {
        return adv20;
    }

    public long getMdv21() {
        return mdv21;
    }

    public double getCcp() {
        return ccp;
    }

    public double getDaccp() {
        return daccp;
    }

    public double getTickPeriod() {
        return tickPeriod;
    }
}
