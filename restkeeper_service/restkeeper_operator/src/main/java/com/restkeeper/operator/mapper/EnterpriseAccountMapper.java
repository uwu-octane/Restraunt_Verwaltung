package com.restkeeper.operator.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.restkeeper.operator.entity.EnterpriseAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface EnterpriseAccountMapper extends BaseMapper<EnterpriseAccount> {
    //recovery del enterprise account
    @Update("update t_enterprise_account set is_deleted=0 where enterprise_id=#{id} and is_deleted=1")
    boolean recovery(@Param("id") String id);

    @Select("SELECT * FROM t_enterprise_account WHERE enterprise_id = #{id}")
    EnterpriseAccount selectByIdWithDeleted(@Param("id") String id);
}
