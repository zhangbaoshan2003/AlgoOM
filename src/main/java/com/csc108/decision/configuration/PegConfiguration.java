package com.csc108.decision.configuration;

import com.csc108.decision.IDecisionConfig;
import com.csc108.decision.pegging.PegCaptureMode;
import com.csc108.decision.pegging.PegRiskFactor;
import org.jdom2.Element;
import quickfix.fix42.NewOrderSingle;

/**
 * Created by zhangbaoshan on 2016/8/1.
 */

public class PegConfiguration implements IDecisionConfig {
    public static final int PegDisplaySizeField = 15012;

    public static PegConfiguration build(NewOrderSingle newOrderSingle){
        if(newOrderSingle.isSetField(PegDisplaySizeField)){
            return new PegConfiguration();
        }
        return null;
    }

    private int displaySize=1000;
    private PegRiskFactor riskFactor = PegRiskFactor.UltraHigh;
    private PegCaptureMode captureMode = PegCaptureMode.Aggressive;
    private int ladderTicks=1;
    private int ladderLevel=3;
    private int pegReplenishPct=50;

    public PegConfiguration(){

    }

    public int getDisplaySize() {
        return displaySize;
    }

    public void setDisplaySize(int displaySize) {
        this.displaySize = displaySize;
    }

    public PegRiskFactor getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(PegRiskFactor riskFactor) {
        this.riskFactor = riskFactor;
    }

    public PegCaptureMode getCaptureMode() {
        return captureMode;
    }

    public void setCaptureMode(PegCaptureMode captureMode) {
        this.captureMode = captureMode;
    }

    public int getLadderTicks() {
        return ladderTicks;
    }

    public void setLadderTicks(int ladderTicks) {
        this.ladderTicks = ladderTicks;
    }

    public int getLadderLevel() {
        return ladderLevel;
    }

    public void setLadderLevel(int ladderLevel) {
        this.ladderLevel = ladderLevel;
    }

    public int getPegReplenishPct() {
        return pegReplenishPct;
    }

    public void setPegReplenishPct(int pegReplenishPct) {
        this.pegReplenishPct = pegReplenishPct;
    }

    public String toString(){
        return String.format("display size:%d ladderLevel:%d",displaySize,ladderLevel);
    }

    @Override
    public void init(Element configNode) {

    }
}