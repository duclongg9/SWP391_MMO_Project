package dao;

import model.WithdrawalRejectionReasons;

public interface WithdrawalRejectionReasonsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(WithdrawalRejectionReasons row);
    int insertSelective(WithdrawalRejectionReasons row);
    WithdrawalRejectionReasons selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(WithdrawalRejectionReasons row);
    int updateByPrimaryKey(WithdrawalRejectionReasons row);
}