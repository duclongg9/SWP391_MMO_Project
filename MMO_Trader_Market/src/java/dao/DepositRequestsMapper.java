package dao;

import model.DepositRequests;

public interface DepositRequestsMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(DepositRequests row);

    int insertSelective(DepositRequests row);

    DepositRequests selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(DepositRequests row);

    int updateByPrimaryKeyWithBLOBs(DepositRequests row);
    int updateByPrimaryKey(DepositRequests row);
}