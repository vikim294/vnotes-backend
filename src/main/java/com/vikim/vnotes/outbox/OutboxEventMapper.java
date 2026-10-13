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

    // 找 10 条：PENDING 或 SENDING 且已领取但过期的
    @Select("""
            select *
            from outbox_events
            where status = 'PENDING'
                or (
                    status = 'SENDING'
                    and claimed_at < now() - interval 5 min
                )
            order by created_at, id
            limit 10
            for update skip locked
            """)
    List<OutboxEvent> fetchPendingOutboxEventsForUpdate();

    int pickJobs(@Param("eventIds") List<String> eventIds);

    @Update("""
            update outbox_events
            set status = 'FAILED',
                claimed_at = null
            where event_id = #{eventId}
                and status = 'SENDING'
                and claim_version = #{claimVersion}
            """)
    int markFailed(@Param("eventId") String eventId, @Param("claimVersion") Long claimVersion);

    @Update(""" 
            update outbox_events
            set status = 'PENDING',
                claimed_at = null
            where event_id = #{eventId}
                and status = 'SENDING'
                and claim_version = #{claimVersion}
            """)
    int markPending(@Param("eventId") String eventId, @Param("claimVersion") Long claimVersion);

    @Update("""
            update outbox_events
            set status = 'SENT',
                claimed_at = null
            where event_id = #{eventId}
                and status = 'SENDING'
                and claim_version = #{claimVersion}
            """)
    int markSent(@Param("eventId") String eventId, @Param("claimVersion") Long claimVersion);
}
