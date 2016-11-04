package com.csc108.tradingRule.core;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by LEGEN on 2016/10/29.
 */
public interface IRule {
    /// <summary>
    /// Name of the rule entry.
    /// </summary>
    String getRuleName();

    /// <summary>
    /// The list of evaluators and their criterias
    /// IList<KeyValuePair<EvaluatorName, Criteria>
    /// </summary>
    List<HashMap<String,String>> getEvaluatorCriterias();

    /// <summary>
    /// The list of handlers and their corresponding parameters.
    /// IList<KeyValuePair<HandlerName, IDictionary<ParamName, ParamValue>>>
    /// </summary>
    List<HashMap<String,LinkedHashMap<String,String>>> getHandlerParameters();
}
