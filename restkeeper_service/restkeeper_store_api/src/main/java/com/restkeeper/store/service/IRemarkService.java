package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Remark;

import java.util.List;

public interface IRemarkService extends IService<Remark> {


    List<Remark> getRemarks();

    boolean updateRemarks(List<Remark> remarks);
}
