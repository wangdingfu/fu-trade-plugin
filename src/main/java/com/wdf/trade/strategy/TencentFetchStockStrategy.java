package com.wdf.trade.strategy;

import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 从腾讯api获取股票数据
 */
@Slf4j
public class TencentFetchStockStrategy implements FetchStockStrategy{
    private static final String BASE_URL = "http://qt.gtimg.cn/q=";

    @Override
    public FetchStockSourceEnum source() {
        return FetchStockSourceEnum.TENCENT;
    }

    @Override
    public List<StockInfo> fetch(List<String> codeList) {
        String codeStr = String.join(",", codeList);
        String requestUrl = BASE_URL + codeStr;;
        try {
            return parseStockSegment(HttpUtil.get(requestUrl));
        }catch (Exception e){
            log.warn("从东方财富获取股票实时信息异常:{}",e.getMessage());
        }
        return new ArrayList<>();
    }


    private static List<StockInfo> parseStockSegment(String result) {
        List<StockInfo> stockInfoList = new ArrayList<>();
        String[] lines = result.split("\n");
        for (String line : lines) {
            String code = line.substring(line.indexOf("_") + 1, line.indexOf("="));
            String dataStr = line.substring(line.indexOf("=") + 2, line.length() - 2);
            String[] values = dataStr.split("~");
            StockInfo bean = new StockInfo();
            bean.setStockCode(code);
            bean.setStockName(values[1]);
            bean.setCurrentPrice(values[3]);
            bean.setIncreaseRate(values[31]);
            stockInfoList.add(bean);
        }
        return stockInfoList;
    }
}
