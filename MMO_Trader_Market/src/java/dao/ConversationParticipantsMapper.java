package dao;

import model.ConversationParticipants;
import org.apache.ibatis.annotations.Param;

public interface ConversationParticipantsMapper {

    int deleteByPrimaryKey(@Param("conversationId") Integer conversationId, @Param("userId") Integer userId);

    int insert(ConversationParticipants row);
    int insertSelective(ConversationParticipants row);
}