package com.silita.biaodaa.analysisRules.preAnalysis;

import com.silita.biaodaa.analysisRules.inter.PreAnalysisRule;
import com.silita.biaodaa.utils.ChineseCompressUtil;
import com.silita.biaodaa.utils.MyStringUtils;
import com.snatch.model.EsNotice;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dh on 2018/4/2.
 */
@Component
public class SplitSection implements PreAnalysisRule {

    @Override
    public String[] buildAnalysisList(EsNotice esNotice,String split1,String split2)throws Exception{
        if(MyStringUtils.isNull(esNotice.getContent())){
            throw new Exception("公告内容为空。[esNotice.getContent():"+esNotice.getContent()+"]");
        }
        ChineseCompressUtil util = new ChineseCompressUtil();
        String plainHtml = util.getPlainText(esNotice.getContent());
        String[] str = plainHtml.split(split1);
        List<String> list = Arrays.asList(str);
        List<String> mainList = new ArrayList<>();
        for(String temp : list){
            String [] str2 = temp.split(split2);
            List<String> list2 = Arrays.asList(str2);
            mainList.addAll(list2);
        }
        String[] array = new String[mainList.size()];
        String [] main = mainList.toArray(array);
        if(main.length>1) {
            main = Arrays.copyOf(main, main.length + 1);
            main[main.length - 1] = plainHtml;
        }
        return main;
    }
}
