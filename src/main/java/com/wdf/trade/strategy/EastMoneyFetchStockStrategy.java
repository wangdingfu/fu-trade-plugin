package com.wdf.trade.strategy;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 从东方财富api获取股票数据
 */
@Slf4j
public class EastMoneyFetchStockStrategy implements FetchStockStrategy{

    private static final String API_URL = "http://push2.eastmoney.com/api/qt/ulist.np/get?secids=%s&fields=f12,f14,f43,f46,f57";

    @Override
    public FetchStockSourceEnum source() {
        return FetchStockSourceEnum.EAST_MONEY;
    }

    @Override
    public List<StockInfo> fetch(List<String> codeList) {
        // 1. 拼接secids参数（如"1.600519,0.000858"）
        String secIdStr = String.join(",", codeList);
        String requestUrl = String.format(API_URL, secIdStr);
        try {
            return parseJsonToStockList(HttpUtil.get(requestUrl));
        }catch (Exception e){
            log.warn("从东方财富获取股票实时信息异常:{}",e.getMessage());
        }
        return Lists.newArrayList();
    }


    /**
     * 解析东方财富返回的JSON为Stock列表
     * @param jsonStr API返回的JSON字符串
     * @return Stock列表
     */
    private static List<StockInfo> parseJsonToStockList(String jsonStr) {
        List<StockInfo> stockList = new ArrayList<>();
        JSONObject rootObj = JSONUtil.parseObj(jsonStr);
        // 逐层解析JSON结构（东方财富返回格式固定：data -> diff -> 数组）
        JSONObject dataObj = rootObj.getJSONObject("data");
        if (dataObj == null) return stockList;

        JSONArray diffArray = dataObj.getJSONArray("diff");
        if (diffArray == null || diffArray.isEmpty()) return stockList;

        // 遍历股票数据数组，转换为Stock对象
        for (Object obj : diffArray) {
            JSONObject stockObj = (JSONObject) obj;
            StockInfo stock = new StockInfo();

            // 提取字段并赋值（注意处理null值，避免空指针）
            stock.setStockCode(stockObj.getStr("f12") == null ? "" : stockObj.getStr("f12"));
            stock.setStockName(stockObj.getStr("f14") == null ? "" : stockObj.getStr("f14"));
            stock.setCurrentPrice(stockObj.getStr("f43") == null ? "0.00" : stockObj.getStr("f43"));
            stock.setIncreaseRate(stockObj.getStr("f46") == null ? "0.00" : stockObj.getStr("f46"));

            // 成交量转换：东方财富返回“手”，需除以10000转为“万手”，保留2位小数
            String volumeHand = stockObj.getStr("f57");
            if (volumeHand == null || volumeHand.isEmpty()) {
                stock.setVolume("0.00");
            } else {
                double volumeWan = Double.parseDouble(volumeHand) / 10000;
                stock.setVolume(String.format("%.2f", volumeWan));
            }
            stockList.add(stock);
        }
        return stockList;
    }
}
