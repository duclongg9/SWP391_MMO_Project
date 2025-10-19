package dao;

import model.Orders;

public interface OrdersMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(Orders row);
    int insertSelective(Orders row);
    Orders selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Orders row);
    int updateByPrimaryKey(Orders row);
}