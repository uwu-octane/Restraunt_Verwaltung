package com.restkeeper.store.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.restkeeper.constants.SystemCode;
import com.restkeeper.store.entity.Credit;
import com.restkeeper.store.entity.CreditCompanyUser;
import com.restkeeper.store.mapper.CreditMapper;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service("creditService")
@Service(version = "1.0.0",protocol = "dubbo")
public class CreditServiceImpl extends ServiceImpl<CreditMapper, Credit> implements ICreditService {

    @Autowired
    @Qualifier("creditCompanyUserService")
    private ICreditCompanyUserService creditCompanyUserService;


    @Override
    @Transactional
    public boolean add(Credit credit, List<CreditCompanyUser> users) {
        this.save(credit);
        if(users != null && !users.isEmpty()){
            List<String> userNameList = users.stream().map(d->d.getUserName()).collect(Collectors.toList());
            long count = userNameList.stream().distinct().count();
            if (userNameList.size() != count){
                throw new RuntimeException("Duplicate User Name");
            }

            users.forEach(d->{
                d.setCreditId(credit.getCreditId());
            });
            return creditCompanyUserService.saveBatch(users);
        }
        return true;
    }

    @Override
    public IPage<Credit> queryPage(int pageNum, int pageSize, String userName) {
        IPage<Credit> page = new Page<>(pageNum,pageSize);
        QueryWrapper<Credit> queryWrapper = new QueryWrapper<>();

        queryWrapper.lambda().like(Credit::getUserName, userName).or().inSql(Credit::getCreditId,
                "select credit_id from t_credit_company_user where user_name like '%"+ StringEscapeUtils.escapeSql(userName) +"%'");
        page = this.page(page,queryWrapper);
        List<Credit> creditList = page.getRecords();
        creditList.forEach(d->{
            if (d.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
                QueryWrapper<CreditCompanyUser> creditCompanyUserQueryWrapper = new QueryWrapper<>();
                creditCompanyUserQueryWrapper.lambda().eq(CreditCompanyUser::getCreditId, d.getCreditId());
                d.setUsers(creditCompanyUserService.list(creditCompanyUserQueryWrapper));
            }
        });
        return page;
    }

    @Override
    public Credit queryById(String id) {
        Credit credit = this.getById(id);
        if (credit ==null) {
            throw new RuntimeException("Record not exist");
        }

        if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY) {
            QueryWrapper<CreditCompanyUser> creditCompanyUserQueryWrapper = new QueryWrapper<>();
            creditCompanyUserQueryWrapper.lambda().eq(CreditCompanyUser::getCreditId, credit.getCreditId());

            credit.setUsers(creditCompanyUserService.list(creditCompanyUserQueryWrapper));
        }
        return  credit;
    }

    @Override
    @Transactional
    public boolean updateCredit(Credit credit, List<CreditCompanyUser> users) {
        if (credit.getCreditType() == SystemCode.CREDIT_TYPE_COMPANY){
            List<CreditCompanyUser> userList = credit.getUsers();
            if (userList != null && !userList.isEmpty()){
                List<String> idList = userList.stream().map(d->d.getId()).collect(Collectors.toList());
                creditCompanyUserService.removeByIds(idList);
            }
        }

        if (users != null && !users.isEmpty()) {
            List<String> userNameList = users.stream().map(d->d.getUserName()).collect(Collectors.toList());

            long count = userNameList.stream().distinct().count();
            if (userNameList.size() != count) {
                throw new RuntimeException("duplicate user name");
            }
            users.forEach(d->d.setCreditId(credit.getCreditId()));
            return creditCompanyUserService.saveBatch(users);
        }

        return this.saveOrUpdate(credit);
    }


}
