package dao;

import model.Conversations;

public interface ConversationsMapper {

    int deleteByPrimaryKey(Integer id);

    int insert(Conversations row);

    int insertSelective(Conversations row);


    Conversations selectByPrimaryKey(Integer id);


    int updateByPrimaryKeySelective(Conversations row);


    int updateByPrimaryKey(Conversations row);
}