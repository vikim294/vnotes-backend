package com.vikim.vnotes.processedmessage;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProcessedMessageMapper {

    @Insert("""
            insert into processed_messages(event_id, consumer)
            values(#{eventId},#{consumer})
            """)
    int createProcessedMessage(@Param("eventId") String eventId, @Param("consumer") String consumer);
}
