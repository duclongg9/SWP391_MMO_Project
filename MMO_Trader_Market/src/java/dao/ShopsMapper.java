package dao;

import model.Shops;

public interface ShopsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(Shops row);
    int insertSelective(Shops row);
    Shops selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Shops row);
    int updateByPrimaryKeyWithBLOBs(Shops row);
    int updateByPrimaryKey(Shops row);
}