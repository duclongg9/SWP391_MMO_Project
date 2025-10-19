package dao;

import model.KycRequestStatuses;

public interface KycRequestStatusesMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(KycRequestStatuses row);

    int insertSelective(KycRequestStatuses row);

    KycRequestStatuses selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(KycRequestStatuses row);

    int updateByPrimaryKey(KycRequestStatuses row);
}