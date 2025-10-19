package dao;

import model.Users;

public interface UsersMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(Users row);
    int insertSelective(Users row);
    Users selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Users row);
    int updateByPrimaryKey(Users row);
}