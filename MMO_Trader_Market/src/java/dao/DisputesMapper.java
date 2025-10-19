package dao;

import model.Disputes;

public interface DisputesMapper {

    int deleteByPrimaryKey(Integer id);


    int insert(Disputes row);


    int insertSelective(Disputes row);

    Disputes selectByPrimaryKey(Integer id);


    int updateByPrimaryKeySelective(Disputes row);

    int updateByPrimaryKeyWithBLOBs(Disputes row);

    int updateByPrimaryKey(Disputes row);
}