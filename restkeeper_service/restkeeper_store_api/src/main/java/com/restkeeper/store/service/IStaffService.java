package com.restkeeper.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.restkeeper.store.entity.Staff;
import com.restkeeper.utils.Result;

public interface IStaffService extends IService<Staff> {
    //add new staff info
    boolean addStaff(Staff staff);

    Result loginStaff(String shopId, String loginName, String loginPass);
}
