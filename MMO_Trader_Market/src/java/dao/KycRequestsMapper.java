package dao;

import model.KycRequests;

public interface KycRequestsMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(KycRequests row);


    int insertSelective(KycRequests row);

    KycRequests selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(KycRequests row);

    int updateByPrimaryKeyWithBLOBs(KycRequests row);

    int updateByPrimaryKey(KycRequests row);
}