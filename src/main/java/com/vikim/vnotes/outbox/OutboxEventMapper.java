package com.vikim.vnotes.outbox;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OutboxEventMapper {

    @Insert("""
            insert into outbox_events(event_id, event_name, event_json, status)
            values(#{eventId}, #{eventName}, #{eventJson}, #{status})
            """)
    int createOutboxEvent(CreateOutboxEventMapperParam param);

    @Select("""
            select *
            from outbox_events
            where status = 'PENDING'
            order by created_at, id
            limit 10
            """)
    List<OutboxEvent> fetchPendingOutboxEvents();

    @Update("""
            update outbox_events
            set status = 'SENT'
            where event_id = #{eventId}
            """)
    int markSent(@Param("eventId") String eventId);
}


