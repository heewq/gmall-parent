package com.atguigu.gmall.common.retryer;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Retryer3 implements Retryer {
    int count = 3;
    int num = 0;

    /**
     * 传播错误
     *
     * @param e
     */
    @Override
    public void continueOrPropagate(RetryableException e) {
        /*
        0 >= 3
        1 >= 3
        2 >= 3
        3 >= 3 throw e
         */
        if (num++ >= 3) {
            throw e;
        } else
            log.info("正在重试第 {} 次", num);
    }

    @Override
    public Retryer clone() {
        return new Retryer3();
    }
}
