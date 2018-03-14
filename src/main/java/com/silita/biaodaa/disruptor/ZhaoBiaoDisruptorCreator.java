package com.silita.biaodaa.disruptor;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.silita.biaodaa.disruptor.event.AnalyzeEvent;
import com.silita.biaodaa.disruptor.exception.AnalyzeException;
import com.silita.biaodaa.disruptor.handler.zhaoBiao.ApplyDateHandler;
import com.silita.biaodaa.disruptor.handler.zhaoBiao.ApplyProjSumHandler;
import com.silita.biaodaa.disruptor.handler.zhaoBiao.InsertAnalyzeDetailHandler;
import com.silita.biaodaa.disruptor.handler.zhaoBiao.TbAssureSumHandler;
import com.silita.biaodaa.service.NoticeAnalyzeService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: caigang
 * Time: 2015/8/10 15:06
 * Remark: 创建disruptor
 */
@Component
public class ZhaoBiaoDisruptorCreator {

    @Autowired
    NoticeAnalyzeService noticeAnalyzeService;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ZhaoBiaoDisruptorCreator.class);

    private static Disruptor<AnalyzeEvent> processDisruptor;

    private static final EventFactory<AnalyzeEvent> EVENT_FACTORY = new EventFactory<AnalyzeEvent>() {
        @Override
        public AnalyzeEvent newInstance() {
            return new AnalyzeEvent();
        }
    };

    private static final int BUFFER_SIZE = 1024;

    //TODO 需要根据其他因素调整线程数量
    private static final int THREAD_NUM = 6;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(THREAD_NUM);

    /**
     * 利用spring完成初始化，singleton
     */
    public static synchronized void initDisruptor(TbAssureSumHandler tbAssureSumHandler
            ,ApplyProjSumHandler applyProjSumHandler
            ,ApplyDateHandler applyDateHandler
            ,InsertAnalyzeDetailHandler insertAnalyzeDetailHandler) {
        if(processDisruptor == null) {
            logger.info("..........disruptor init..........\nDISRUPTOR BUFFER_SIZE:"+BUFFER_SIZE+" THREAD_NUM:"+THREAD_NUM);
            processDisruptor = new Disruptor<AnalyzeEvent>(EVENT_FACTORY,BUFFER_SIZE,EXECUTOR,ProducerType.SINGLE,new SleepingWaitStrategy());
            processDisruptor.handleExceptionsWith(new AnalyzeException());
            processDisruptor.handleEventsWith(tbAssureSumHandler)
                    .then(applyProjSumHandler)
                    .then(applyDateHandler)
                    .then(insertAnalyzeDetailHandler);
            logger.info("..........disruptor init success..........");
        }
    }

    public static Disruptor<AnalyzeEvent> getProcessDisruptor() {
        return processDisruptor;
    }

    public static void shutdownDisruptor() {
        processDisruptor.shutdown();
        EXECUTOR.shutdown();
    }
}