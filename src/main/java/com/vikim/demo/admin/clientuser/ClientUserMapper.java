package com.vikim.demo.admin.clientuser;

import com.vikim.demo.user.UserStatus;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ClientUserMapper {
    // 先不分页
    @Select("""
            select
                u.id,
                u.username,
                u.email,
                u.avatar_url,
                u.status,
                count(distinct dir.id) as note_count,
                count(distinct ns.id) as shared_note_count,
                count(distinct ar.id) as article_count,
                u.created_at
            from users u
            left join directory_nodes dir on dir.user_id = u.id
                and dir.deleted_at is null
                and dir.node_type = 'DETAIL'
            left join note_shares ns on ns.directory_node_id = dir.id
                and ns.status = 'ACTIVE'
            left join detail_nodes de on de.directory_node_id = dir.id
            left join articles ar on ar.detail_node_id = de.id
            group by u.id
            order by u.created_at desc, u.id desc
            """)
    List<GetClientUserRow> getClientUserList();

    @Update("""
            update users
            set status = #{status}
            where id = #{userId}
                and status != #{status}
            """)
    int enableOrDisableClientUser(@Param("userId") Long userId, @Param("status") UserStatus status);

    @Select("""
            select exists(
                select 1
                from users
                where id = #{userId}
            )
            """)
    boolean existsById(Long userId);
}
