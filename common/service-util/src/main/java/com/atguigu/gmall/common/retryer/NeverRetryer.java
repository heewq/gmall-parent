package com.atguigu.gmall.common.retryer;

import feign.RetryableException;
import feign.Retryer;

public class NeverRetryer implements Retryer {
    @Override
    public void continueOrPropagate(RetryableException e) {
        throw e;
    }

    @Override
    public Retryer clone() {
        return new NeverRetryer();
    }
}
