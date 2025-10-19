package dao;

import model.Wallets;

public interface WalletsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(Wallets row);
    int insertSelective(Wallets row);
    Wallets selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Wallets row);
    int updateByPrimaryKey(Wallets row);
}