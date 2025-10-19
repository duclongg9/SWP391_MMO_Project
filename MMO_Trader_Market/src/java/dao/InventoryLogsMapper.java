package dao;

import model.InventoryLogs;

public interface InventoryLogsMapper {

    int deleteByPrimaryKey(Long id);


    int insert(InventoryLogs row);


    int insertSelective(InventoryLogs row);

    InventoryLogs selectByPrimaryKey(Long id);


    int updateByPrimaryKeySelective(InventoryLogs row);

    int updateByPrimaryKey(InventoryLogs row);
}