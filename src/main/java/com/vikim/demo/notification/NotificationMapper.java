package com.vikim.demo.notification;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface NotificationMapper {

    // 先不分页
    // 未来做滚动加载更多，可能用 cursor
    @Select("""
            select id, type, title, content, has_read, created_at
            from notifications
            where user_id = #{userId} and deleted_at is null
            order by created_at desc, id desc
            """)
    List<FetchUserNotificationsResponse> fetchUserNotifications(@Param("userId") Long userId);


    @Insert("""
            insert into notifications(type, user_id, title, content)
            values(#{type},#{userId},#{title},#{content})
            """)
    int createNotificationForUser(@Param("type") NotificationType type, @Param("userId") Long userId, @Param("title") String title, @Param("content") String content);
}
