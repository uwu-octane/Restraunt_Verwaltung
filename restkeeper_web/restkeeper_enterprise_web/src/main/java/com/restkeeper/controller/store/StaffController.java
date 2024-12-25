package com.restkeeper.controller.store;

import com.restkeeper.store.entity.Staff;
import com.restkeeper.store.service.IStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(tags = {"Staff Management"})
@RestController
@RequestMapping("/Staff")
public class StaffController {
    @Reference(version = "1.0.0", check=false)
    private IStaffService staffService;

    @ApiOperation(value = "新增员工")
    @PostMapping(value = "/addNewStaff")
    public boolean addNewStaff(@RequestBody Staff staff){
        return staffService.addStaff(staff);
    }
}
