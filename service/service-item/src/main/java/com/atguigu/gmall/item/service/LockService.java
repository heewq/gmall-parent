package com.atguigu.gmall.item.service;

public interface LockService {
    String lock();

    void unLock(String uuid);
}
