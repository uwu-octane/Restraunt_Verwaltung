package com.restkeeper.controller.store;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.response.vo.PageVO;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.entity.CreditLogs;
import com.restkeeper.store.entity.CreditRepayment;
import com.restkeeper.store.service.ICreditLogService;
import com.restkeeper.store.service.ICreditRepaymentService;
import com.restkeeper.store.service.ICreditService;
import com.restkeeper.utils.BeanListUtils;
import com.restkeeper.vo.store.CreditLogExcelVO;
import com.restkeeper.vo.store.CreditRepaymentVO;
import com.restkeeper.vo.store.CreditVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/credit")
@Api(tags = {"Credit Management"})
public class CreditController {

    @Reference(version = "1.0.0",check = false)
    private ICreditService creditService;

    @Reference(version = "1.0.0",check = false)
    private ICreditLogService creditLogService;

    @Reference(version = "1.0.0", check=false)
    private ICreditRepaymentService creditRepaymentService;

    @ApiOperation(value = "add new credit record")
    @PutMapping("/add")
    public boolean add(@RequestBody CreditVO creditVO){
        Credit credit = new Credit();
        BeanUtils.copyProperties(creditVO, credit, "users");

        if (creditVO.getUsers() != null && !creditVO.getUsers().isEmpty()){
            List<CreditCompanyUser> companyUserList = Lists.newArrayList();
            creditVO.getUsers().forEach(d->{
                CreditCompanyUser creditCompanyUser = new CreditCompanyUser();
                BeanUtils.copyProperties(d,creditCompanyUser);
                companyUserList.add(creditCompanyUser);
            });
            return creditService.add(credit,companyUserList);
        }
        return creditService.add(credit,null);
    }

    @ApiOperation(value = "Credit Management in List")
    @GetMapping("/pageList/{pageNum}/{pageSize}")
    public PageVO<CreditVO> pageList(@RequestParam(value="name", defaultValue = "") String name, @PathVariable int pageNum, @PathVariable int pageSize){
        IPage<Credit> creditIPage = creditService.queryPage(pageNum,pageNum,name);

        List<CreditVO> voList = Lists.newArrayList();
        try {
            voList = BeanListUtils.copy(creditIPage.getRecords(), CreditVO.class);
        } catch (Exception e){
            throw new RuntimeException("List convert failed, call in listing page of credit.");
        }

        return new PageVO<CreditVO>(creditIPage,voList);
    }

    @ApiOperation(value = "get credit record by id")
    @GetMapping("/{id}")
    public CreditVO getCreditById(@PathVariable String id){
        CreditVO creditVO = new CreditVO();
        Credit credit = creditService.queryById(id);
        BeanUtils.copyProperties(credit, creditVO);
        return creditVO;
    }

    @ApiOperation(value = "update credit info")
    @PostMapping("/updateCredit/{id}")
    public boolean updateCredit(@PathVariable String id, @RequestBody CreditVO creditVO){
        Credit credit = creditService.queryById(id);
        BeanUtils.copyProperties(creditVO,credit, "users");

        if(creditVO.getUsers() != null && !creditVO.getUsers().isEmpty()){
            List<CreditCompanyUser> companyUserList = Lists.newArrayList();
            creditVO.getUsers().forEach(d->{
                CreditCompanyUser creditCompanyUser = new CreditCompanyUser();
                BeanUtils.copyProperties(d,creditCompanyUser);
                companyUserList.add(creditCompanyUser);
            });
            return creditService.updateCredit(credit,companyUserList);
        }
        return  creditService.updateCredit(credit,null);
    }

    @ApiOperation(value = "credit order info list")
    @GetMapping("/creditLog/{pageNum}/{pageSize}/{creditId}")
    public PageVO<CreditLogs> getCreditLogPageList( @RequestParam(value = "creditId") String creditId, @PathVariable int pageNum,
                                                   @PathVariable int pageSize){
        return new PageVO<CreditLogs>(creditLogService.queryPage(creditId,pageNum,pageSize));
    }

    //export log to excel
    @GetMapping("/export/creditId/{creditId}/start/{start}/end/{end}")
    public  void exportExcel(HttpServletResponse response,
                             @PathVariable String creditId,
                             @PathVariable String start,
                             @PathVariable String end) throws IOException {
        //time format
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);

        if(startTime.isAfter(endTime)){
            throw new RuntimeException("start time after end time ");
        }

        List<CreditLogExcelVO> data = creditLogService
                .listCreditLog(creditId,startTime,endTime)
                .stream()
                .map(c->{
                    CreditLogExcelVO orderVO = new CreditLogExcelVO();
                    orderVO.setCreditAmount(c.getCreditAmount());
                    orderVO.setDateTime(Date.from( c.getLastUpdateTime().atZone( ZoneId.systemDefault()).toInstant()));
                    orderVO.setOrderAmount(c.getOrderAmount());
                    orderVO.setRevenueAmount(c.getReceivedAmount());
                    orderVO.setUserName(c.getUserName());
                    orderVO.setOrderId(c.getOrderId());
                    if(c.getType()== SystemCode.CREDIT_TYPE_COMPANY){
                        orderVO.setCreditType("企业");
                    }else {
                        orderVO.setCreditType("个人");
                    }
                    return orderVO;
                }).collect(Collectors.toList());

        //set header info
        //
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("demo", "utf-8");
        response.setHeader("Content-disposition", "attachment;filename="+fileName+".xlsx");
        EasyExcel.write(response.getOutputStream(), CreditLogExcelVO.class).sheet("模板").doWrite(data);
    }

    @ApiOperation(value = "还款")
    @PostMapping("/repayment")
    public boolean repayment(@RequestBody CreditRepaymentVO creditRepaymentVo){
        CreditRepayment creditRepayment =new CreditRepayment();
        BeanUtils.copyProperties(creditRepaymentVo,creditRepayment);
        return creditRepaymentService.repayment(creditRepayment);
    }
}
