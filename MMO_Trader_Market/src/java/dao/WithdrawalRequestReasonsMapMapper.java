package dao;

import model.WithdrawalRequestReasonsMap;
import org.apache.ibatis.annotations.Param;

public interface WithdrawalRequestReasonsMapMapper {

    int deleteByPrimaryKey(@Param("requestId") Integer requestId, @Param("reasonId") Integer reasonId);
    int insert(WithdrawalRequestReasonsMap row);
    int insertSelective(WithdrawalRequestReasonsMap row);
}