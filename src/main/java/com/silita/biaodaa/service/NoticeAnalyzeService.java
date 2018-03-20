package com.silita.biaodaa.service;

import com.silita.biaodaa.cache.GlobalCache;
import com.silita.biaodaa.common.Constant;
import com.silita.biaodaa.dao.AnalyzeRangeMapper;
import com.silita.biaodaa.utils.CNNumberFormat;
import com.silita.biaodaa.utils.ChineseCompressUtil;
import com.silita.biaodaa.utils.DateUtils;
import com.silita.biaodaa.utils.MyStringUtils;
import com.snatch.model.AnalyzeDetail;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhangxiahui on 18/3/13.
 */
@Component
public class NoticeAnalyzeService {

    Logger logger = Logger.getLogger(NoticeAnalyzeService.class);

    @Autowired
    AnalyzeRangeMapper analyzeRangeMapper;

    private ChineseCompressUtil chineseCompressUtil = new ChineseCompressUtil();


    /**
     * 解析保证金
     * 正则匹配
     * return 保证金
     */
    public String analyzeApplyDeposit(String html) {
        String rangeHtml="";
        String deposit = "";

        Map<String,List<Map<String, Object>>> analyzeRangeByFieldMap = GlobalCache.getGlobalCache().getAnalyzeRangeByFieldMap();
        List<Map<String, Object>> arList = analyzeRangeByFieldMap.get("applyDeposit");
        if(arList == null){
            arList = analyzeRangeMapper.queryAnalyzeRangeByField("applyDeposit");
            analyzeRangeByFieldMap.put("applyDeposit",arList);
            GlobalCache.getGlobalCache().setAnalyzeRangeByFieldMap(analyzeRangeByFieldMap);
        }else{
            logger.info("=========applyDeposit=======走的缓存=======");
        }
        for (int i = 0; i < arList.size(); i++) {
            String start = arList.get(i).get("rangeStart").toString();
            String end = arList.get(i).get("rangeEnd").toString();
            int indexStart=0;
            int indexEnd=0;
            if(!"".equals(start)){
                indexStart = html.indexOf(start);//范围开始位置
            }
            if(!"".equals(end)){
                indexEnd = html.indexOf(end);//范围结束位置
            }
            if(indexStart > -1 && indexEnd> -1){
                if(indexEnd > indexStart){
                    rangeHtml = html.substring(indexStart, indexEnd+1);//截取范围之间的文本
                }else if(indexStart > indexEnd) {
                    if(html.length()-indexStart>=50){
                        rangeHtml = html.substring(indexStart, indexStart+50);//截取范围开始至后30个字符
                    }else{
                        rangeHtml = html.substring(indexStart, html.length());//截取范围开始至后30个字符
                    }
                }
                //匹配中文人民币
                String regExCn = "([零壹贰叁肆伍陆柒捌玖拾佰仟万亿])";//大写人民币
                Pattern pat1 = Pattern.compile(regExCn);
                rangeHtml = rangeHtml.replaceAll("\\s*", "");	//去空格
                Matcher mat1 = pat1.matcher(rangeHtml);
                String bigDeposit="";
                while (mat1.find()) {
                    bigDeposit+=mat1.group();
                }
                if(bigDeposit.length()>0){
                    int cnn = CNNumberFormat.ChnStringToNumber(bigDeposit);
                    if(cnn>=1000){//保证金需大于等于1000
                        deposit = String.valueOf(cnn);
                        break;
                    }
                }

                //匹配阿拉伯人民币
                String regExAr = "\\d+(\\.\\d+)?亿元|\\d+(\\.\\d+)?万元|\\d+(\\.\\d+)?元";//阿拉伯人民币
                Pattern pat2 = Pattern.compile(regExAr);
                Matcher mat2 = pat2.matcher(rangeHtml);
                while (mat2.find()) {
                    if("".equals(deposit)){
                        int cnn=CNNumberFormat.ChnStringToNumber(mat2.group().replaceAll("元", ""));
                        if(cnn>0){
                            deposit = String.valueOf(cnn);
                            if(Double.parseDouble(deposit)<1000){//项目金额需大于等于1000
                                deposit="";
                            }
                        }else{
                            deposit = mat2.group().replaceAll("万", "").replaceAll("元", "");
                            if(Double.parseDouble(deposit)<1000){//项目金额需大于等于1000
                                deposit="";
                            }
                        }
                    }
                }
                if(deposit.length()>0){
                    break;
                }
            }
        }
        return deposit;
    }

    /**
     * 解析项目金额
     * 正则匹配
     * return 项目金额
     */
    public String analyzeApplyProjSum(String html) {
        String rangeHtml="";
        String deposit = "";
        Map<String,List<Map<String, Object>>> analyzeRangeByFieldMap = GlobalCache.getGlobalCache().getAnalyzeRangeByFieldMap();
        List<Map<String, Object>> arList = analyzeRangeByFieldMap.get("applyProjSum");
        if(arList == null){
            arList = analyzeRangeMapper.queryAnalyzeRangeByField("applyProjSum");
            analyzeRangeByFieldMap.put("applyProjSum",arList);
            GlobalCache.getGlobalCache().setAnalyzeRangeByFieldMap(analyzeRangeByFieldMap);
        }else{
            logger.info("=========applyProjSum=======走的缓存=======");
        }
        for (int i = 0; i < arList.size(); i++) {
            String start = arList.get(i).get("rangeStart").toString();
            String end = arList.get(i).get("rangeEnd").toString();
            int indexStart=0;
            int indexEnd=0;
            if(!"".equals(start)){
                indexStart = html.indexOf(start);//范围开始位置
            }
            if(!"".equals(end)){
                indexEnd = html.indexOf(end);//范围结束位置
            }
            if(indexStart > -1 && indexEnd> -1){
                if(indexEnd > indexStart){
                    rangeHtml = html.substring(indexStart, indexEnd+1);//截取范围之间的文本
                }else if(indexStart > indexEnd) {
                    if(html.length()-indexStart>=50){
                        rangeHtml = html.substring(indexStart, indexStart+50);//截取范围开始至后30个字符
                    }else{
                        rangeHtml = html.substring(indexStart, html.length());//截取范围开始至后30个字符
                    }
                }
                //匹配中文人民币
                String regExCn = "([零壹贰叁肆伍陆柒捌玖拾佰仟万亿])";//大写人民币
                Pattern pat1 = Pattern.compile(regExCn);
                rangeHtml = rangeHtml.replaceAll("\\s*", "");	//去空格
                Matcher mat1 = pat1.matcher(rangeHtml);
                String bigDeposit="";
                while (mat1.find()) {
                    bigDeposit+=mat1.group();
                }
                if(bigDeposit.length()>0){
                    int cnn = CNNumberFormat.ChnStringToNumber(bigDeposit);
                    if(cnn>=200000){//项目金额需大于等于200000
                        deposit = String.valueOf(cnn);
                        break;
                    }
                }

                //匹配阿拉伯人民币
                String regExAr = "\\d+(\\.\\d+)?亿元|\\d+(\\.\\d+)?万元|\\d+(\\.\\d+)?元";//阿拉伯人民币
                Pattern pat2 = Pattern.compile(regExAr);
                Matcher mat2 = pat2.matcher(rangeHtml);
                while (mat2.find()) {
                    if("".equals(deposit)){
                        int cnn=CNNumberFormat.ChnStringToNumber(mat2.group().replaceAll("元", ""));
                        if(cnn>0){
                            deposit = String.valueOf(cnn);
                            if(Double.parseDouble(deposit)<200000){//项目金额需大于等于200000
                                deposit="";
                            }
                        }else{
                            deposit = mat2.group().replaceAll("万", "").replaceAll("元", "");
                            if(Double.parseDouble(deposit)<200000){//项目金额需大于等于200000
                                deposit="";
                            }
                        }
                    }
                }
                if(deposit.length()>0){
                    break;
                }
            }
        }
//		if(MyStringUtils.isNotNull(deposit)) {
//			BigDecimal d = new BigDecimal(Double.parseDouble(deposit) / 10000);
//			deposit = "约" + String.valueOf(d) + "万";
//		}
        return deposit;
    }

    /**
     * 解析报名时间
     * 正则匹配
     * return 报名开始和结束
     */
    public List<String> analyzeApplyDate(String html){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");// 设置日期格式
        SimpleDateFormat df1 = new SimpleDateFormat("MM-dd");// 设置日期格式
        List<String> list = new ArrayList<String>();
        String rangeHtml="";

        Map<String,List<Map<String, Object>>> analyzeRangeByFieldMap = GlobalCache.getGlobalCache().getAnalyzeRangeByFieldMap();
        List<Map<String, Object>> arList = analyzeRangeByFieldMap.get("applyDate");
        if(arList == null){
            arList = analyzeRangeMapper.queryAnalyzeRangeByField("applyDate");
            analyzeRangeByFieldMap.put("applyDate",arList);
            GlobalCache.getGlobalCache().setAnalyzeRangeByFieldMap(analyzeRangeByFieldMap);
        }else{
            logger.info("=========applyDate=======走的缓存=======");
        }


        for (int i = 0; i < arList.size(); i++) {
            String start = arList.get(i).get("rangeStart").toString();
            String end = arList.get(i).get("rangeEnd").toString();
            int indexStart=0;
            int indexEnd=0;
            if(!"".equals(start)){
                indexStart = html.indexOf(start);//范围开始位置
            }
            if(!"".equals(end)){
                indexEnd = html.indexOf(end);//范围结束位置
            }
            if(indexStart > -1 && indexEnd> -1){
                if(indexEnd > indexStart){
                    rangeHtml = html.substring(indexStart, indexEnd+1);//截取范围之间的文本
                }else{
                    if(html.length()-indexStart>=80){
                        rangeHtml = html.substring(indexStart, indexStart+80);//截取范围开始至后100个字符
                    }else{
                        rangeHtml = html.substring(indexStart, html.length());//截取范围开始至后100个字符
                    }
                }
                String regEx = "(\\d{4}-\\d{1,2}-\\d{1,2})|(\\d{4}年\\d{1,2}月\\d{1,2})";//匹配日期
                Pattern pat = Pattern.compile(regEx);
                Matcher mat = pat.matcher(rangeHtml);
                list = new ArrayList<String>();
                while (mat.find()) {
                    try {
                        list.add(df.format(df.parse(mat.group().replaceAll("年", "-").replaceAll("月", "-"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                if(list.size()>1){
                    break;
                }
            }
        }
        if(list.size() < 2) {
            for (int i = 0; i < arList.size(); i++) {
                String start = arList.get(i).get("rangeStart").toString();
                String end = arList.get(i).get("rangeEnd").toString();
                int indexStart = 0;
                int indexEnd = 0;
                if (!"".equals(start)) {
                    indexStart = html.indexOf(start);//范围开始位置
                }
                if (!"".equals(end)) {
                    indexEnd = html.indexOf(end);//范围结束位置
                }
                if (indexStart > -1 && indexEnd > -1) {
                    if (indexEnd > indexStart) {
                        rangeHtml = html.substring(indexStart, indexEnd + 1);//截取范围之间的文本
                    } else if (indexStart > indexEnd) {
                        if (html.length() - indexStart >= 60) {
                            rangeHtml = html.substring(indexStart, indexStart + 60);//截取范围开始至后100个字符
                        } else {
                            rangeHtml = html.substring(indexStart, html.length());//截取范围开始至后100个字符
                        }
                    }
                    String regEx = "(\\d{1,2}-\\d{1,2})|(\\d{1,2}月\\d{1,2})";//匹配日期
                    Pattern pat = Pattern.compile(regEx);
                    rangeHtml = rangeHtml.replaceAll("\\s*", "");    //去空格
                    Matcher mat = pat.matcher(rangeHtml);
                    list = new ArrayList<String>();
                    if (rangeHtml.contains("即日起")) {
                        list.add("公告时间");
                        while (mat.find()) {
                            try {
                                list.add(df1.format(df1.parse(mat.group().replaceAll("年", "-").replaceAll("月", "-"))));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        if (list.size() == 2) {
                            break;
                        }
                    } else {
                        while (mat.find()) {
                            try {
                                list.add(df1.format(df1.parse(mat.group().replaceAll("年", "-").replaceAll("月", "-"))));
                            } catch (ParseException e) {
                                e.printStackTrace();
                                continue;
                            }
                        }
                        if (list.size() > 1) {
                            break;
                        }
                    }
                    if (list.size() > 1) {
                        if (DateUtils.compareDateStr(list.get(0), list.get(1)) > 30 ) {
                            list.clear();
                        }
                    }
                    if (list.size() > 1) {
                        break;
                    }
                }
            }
        }
        return list;
    }

    public String analyzeApplyTime(String html) {
        SimpleDateFormat dftime = new SimpleDateFormat("HH:mm");
        String bmEndTime = "";
        String rangeHtml = "";
        Map<String,List<Map<String, Object>>> analyzeRangeByFieldMap = GlobalCache.getGlobalCache().getAnalyzeRangeByFieldMap();
        List<Map<String, Object>> arList = analyzeRangeByFieldMap.get("applyDate");
        if(arList == null){
            arList = analyzeRangeMapper.queryAnalyzeRangeByField("applyDate");
            analyzeRangeByFieldMap.put("applyDate",arList);
            GlobalCache.getGlobalCache().setAnalyzeRangeByFieldMap(analyzeRangeByFieldMap);
        }else{
            logger.info("=========applyDate=======走的缓存=======");
        }
        for (int i = 0; i < arList.size(); i++) {
            String start = arList.get(i).get("rangeStart").toString();
            String end = arList.get(i).get("rangeEnd").toString();
            int indexStart=0;
            int indexEnd=0;
            if(!"".equals(start)){
                indexStart = html.indexOf(start);//范围开始位置
            }
            if(!"".equals(end)){
                indexEnd = html.indexOf(end);//范围结束位置
            }
            if(indexStart > -1 && indexEnd> -1){
                if (indexEnd > indexStart) {
                    rangeHtml = html.substring(indexStart, indexEnd + 1);//截取范围之间的文本
                } else if (indexStart > indexEnd) {
                    if (html.length() - indexStart >= 80) {
                        rangeHtml = html.substring(indexStart, indexStart + 80);//截取范围开始至后100个字符
                    } else {
                        rangeHtml = html.substring(indexStart, html.length());//截取范围开始至后100个字符
                    }
                }
                //匹配时间
                String regTime = "(\\d{1,2}:\\d{2}|(\\d{1,2}时)|\\d{1,2}：\\d{2})";
//                String regTime = "(\\d{1,2}:\\d{1,2})|(\\d{1,2}时)";
                Pattern patTime = Pattern.compile(regTime);
                rangeHtml = rangeHtml.replaceAll("\\s*", "");    //去空格
                Matcher matTime = patTime.matcher(rangeHtml);
                while (matTime.find()) {
                    if ("".equals(bmEndTime)) {
                        try {
                            bmEndTime = dftime.format(dftime.parse(matTime.group().replaceAll("时", ":00").replaceAll("：",":")));    //得到时间
                        } catch (ParseException e) {
                            e.printStackTrace();
                            continue;
                        }
//                        if(bmEndTime != ""){
//                            break;
//                        }
                    }
                }
            }
        }
        return bmEndTime;
    }


    /**
     * 解析保证金汇款方式
     * @param html 公告内容
     * @return 保证金汇款方式
     */
    public String analyzeAssureSumRemit (String html) {
        String assureSumRemit = "";
        String content = chineseCompressUtil.getPlainText(html);
        content = MyStringUtils.deleteHtmlTag(content);
        String[] regexs = {
                "(投标保证金的形式)(:|：).*?(,|。|，)",
                "(交纳方式)(:|：).*?(,|。|，)",
                "(投标保证金交纳方式)(:|：).*?(,|。|，)",
                "(投标保证金缴纳方式)(:|：).*?(,|。|，)",
                "(投标保证金递交方式)(:|：).*?(,|。|，)"
        };
        for (int i = 0; i < regexs.length; i++) {
            Pattern pa = Pattern.compile(regexs[i]);
            Matcher ma = pa.matcher(content);
            if (ma.find()) {
                String txt = ma.group();
                int firstIndex = regexs[i].substring(regexs[i].indexOf("(") + 1 ,regexs[i].indexOf(")")).length() + 1;
                txt = txt.substring(firstIndex);

//                if (txt.contains(":")) {
//                    txt = txt.substring(txt.indexOf(":")+1);
//                } else {
//                    txt = txt.substring(txt.indexOf("：")+1);
//                }

                if (txt.contains("投标保证金")) {
                    txt = txt.substring(0,txt.indexOf("投标保证金"));
                } else if (txt.contains("保证金")) {
                    txt = txt.substring(0,txt.indexOf("保证金"));
                } else {
                    txt = txt.substring(0,txt.length()-1);
                }

                String regex = "[0-9一二三四五六七八九]";
                pa = Pattern.compile(regex);
                ma = pa.matcher(txt);
                if (ma.find()) {
                    txt = txt.substring(0,txt.indexOf(ma.group()));
                }

                if (!"".equals(txt) && !Constant.DEFAULT_STRING.equals(txt)) {
                    assureSumRemit = txt;
                    break;
                }
            }
        }
        if (StringUtils.isBlank(assureSumRemit)) {

        }




        return assureSumRemit;
    }



    public void insertAnalyzeDetail(AnalyzeDetail analyzeDetail){
        analyzeRangeMapper.insertAnalyzeDetail(analyzeDetail);
    }

    public void batchInsertAnalyzeDetail(List<AnalyzeDetail> list){
        Map<String,Object> param = new HashMap<>();
        param.put("list",list);
        analyzeRangeMapper.batchInsertAnalyzeDetail(param);
    }
}
