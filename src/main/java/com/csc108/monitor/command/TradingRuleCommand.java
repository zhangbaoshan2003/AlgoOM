package com.csc108.monitor.command;

import com.csc108.log.LogFactory;
import com.csc108.tradingRule.RuleEngine;
import com.csc108.tradingRule.core.IRule;
import com.csc108.tradingRule.providers.TradingRuleProvider;
import com.csc108.utility.FormattedTable;
import org.apache.commons.cli.CommandLine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by NIUXX on 2016/11/14.
 */
public class TradingRuleCommand extends CommandBase {
    public TradingRuleCommand(){
        options.addOption("i",true,"trading rule id to display");
        options.addOption("n",true,"evaluator name to update");
        options.addOption("c",true,"new criteria to apply");
    }

    @Override
    public String getFirstLevelKey(){
        return "trading";
    }

    @Override
    public String getSecondLevelKey(){
        return "rule";
    }

    public String list(String[] arg){
        try {
            FormattedTable formattedTable = new FormattedTable();
            List<Object> header = new ArrayList<>();
            header.add("Rule ID");
            header.add("Rule Name");
            formattedTable.AddRow(header);

            TradingRuleProvider.getInstance().getTradingRules().forEach(x->{
                List<Object> row = new ArrayList<>();
                row.add(TradingRuleProvider.getInstance().getTradingRules().indexOf(x));
                row.add(x.getRuleName());
                formattedTable.AddRow(row);
            });

            return formattedTable.toString();
        }catch (Exception ex){
            LogFactory.error("List trading rule error!",ex);
            return  "Error happened when processing list command!"+ex;
        }
    }

    public String display(String[] args){
        try {
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("i")==false){
                return "rule id is not provided!";
            }
            int id = Integer.parseInt(cml.getOptionValue("i"));

            if(TradingRuleProvider.getInstance().getTradingRules().size()<=id){
                return String.format("%d exceed the total rules %d !",id,TradingRuleProvider.getInstance().getTradingRules().size());
            }

            IRule rule = TradingRuleProvider.getInstance().getTradingRules().get(id);
            if(rule==null){
                return String.format("Can't find %d trading rule!",id) ;
            }

            //output evaluator
            StringBuilder sb = new StringBuilder();

            final FormattedTable formattedTable = new FormattedTable();
            List<Object> header = new ArrayList<>();
            header.add("Evaluator Name");
            header.add("Evaluator Criteria");
            formattedTable.AddRow(header);
            rule.getEvaluatorCriterias().forEach(x->{
                List<Object> row = new ArrayList<>();
                x.keySet().forEach(kv->{
                    row.add(kv);
                    row.add(x.get(kv));
                });
                formattedTable.AddRow(row);
            });
            sb.append(formattedTable.toString()) ;
            sb.append("\n");
            //output handler
            final FormattedTable formattedTable2 = new FormattedTable();
            header = new ArrayList<>();
            header.add("Handler Name");
            header.add("Parameters");
            formattedTable2.AddRow(header);
            rule.getHandlerParameters().forEach(x->{
                List<Object> row = new ArrayList<>();
                x.keySet().forEach(kv->{
                    row.add(kv);//add handler name
                    StringBuilder sbPara = new StringBuilder();
                    HashMap<String,String> paras = x.get(kv);
                    paras.forEach((k,v)->{
                        sbPara.append(k).append(":").append(v);
                    });
                    row.add(sbPara.toString());
                });
                formattedTable2.AddRow(row);
            });

            sb.append(formattedTable2.toString()) ;
            String result = sb.toString();
            return result;

        }catch (Exception ex){
            LogFactory.error("Display trading rule error!",ex);
            return  "Error happened when Displaying rule !"+ex;
        }
    }

    //update evaluator
    public String ue(String[] args){
        try{
            CommandLine cml= parser.parse(options, args);
            if(cml.hasOption("i")==false){
                return "rule id is not provided!";
            }
            int id = Integer.parseInt(cml.getOptionValue("i"));

            if(TradingRuleProvider.getInstance().getTradingRules().size()<=id){
                return String.format("%d exceed the total rules %d !",id,TradingRuleProvider.getInstance().getTradingRules().size());
            }

            IRule rule = TradingRuleProvider.getInstance().getTradingRules().get(id);
            if(rule==null){
                return String.format("Can't find %d trading rule!",id) ;
            }

            if(cml.hasOption("n")==false){
                return "evaluator name is not provided!";
            }
            String evaluatorName = cml.getOptionValue('n');

            if(cml.hasOption("c")==false){
                return "new criteria to apply is not provided!";
            }
            String newCreteria = cml.getOptionValue("c");

            System.out.printf("Begin to update rule %s's evaluator %s with new certeria %s \n",rule.getRuleName(),evaluatorName,newCreteria);
            RuleEngine.updateRuleEvaluator(rule.getRuleName(),evaluatorName,newCreteria);
            System.out.println("update successfully!");
            return "update successfully!";
        }catch (Exception ex){
            System.err.println(ex);
            return "Error updating rule \n "+ex;
        }
    }
}
