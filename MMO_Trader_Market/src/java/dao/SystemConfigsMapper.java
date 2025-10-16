package dao;

import model.SystemConfigs;

public interface SystemConfigsMapper {
    int deleteByPrimaryKey(Integer id);
    int insert(SystemConfigs row);
    int insertSelective(SystemConfigs row);
    SystemConfigs selectByPrimaryKey(Integer id);
    int updateByPrimaryKeySelective(SystemConfigs row);
    int updateByPrimaryKeyWithBLOBs(SystemConfigs row);
    int updateByPrimaryKey(SystemConfigs row);
}