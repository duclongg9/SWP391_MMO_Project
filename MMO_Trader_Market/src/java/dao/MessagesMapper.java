package dao;

import model.Messages;

public interface MessagesMapper {
    int deleteByPrimaryKey(Long id);
    int insert(Messages row);
    int insertSelective(Messages row);
    Messages selectByPrimaryKey(Long id);
    int updateByPrimaryKeySelective(Messages row);
    int updateByPrimaryKeyWithBLOBs(Messages row);
    int updateByPrimaryKey(Messages row);
}