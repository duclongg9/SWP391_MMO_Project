package dao;

import model.WalletTransactions;

public interface WalletTransactionsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(WalletTransactions row);
    int insertSelective(WalletTransactions row);
    WalletTransactions selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(WalletTransactions row);
    int updateByPrimaryKey(WalletTransactions row);
}