package dao;

import model.Roles;

public interface RolesMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(Roles row);
    int insertSelective(Roles row);
    Roles selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(Roles row);
    int updateByPrimaryKey(Roles row);
}