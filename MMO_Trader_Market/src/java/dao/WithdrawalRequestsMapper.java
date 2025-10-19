package dao;

import model.WithdrawalRequests;

public interface WithdrawalRequestsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(WithdrawalRequests row);
    int insertSelective(WithdrawalRequests row);
    WithdrawalRequests selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(WithdrawalRequests row);
    int updateByPrimaryKeyWithBLOBs(WithdrawalRequests row);
    int updateByPrimaryKey(WithdrawalRequests row);
}