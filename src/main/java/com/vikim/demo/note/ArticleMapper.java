package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.util.Optional;

@Mapper
public interface ArticleMapper {

    @Insert("""
            insert into articles(detail_node_id)
            values(#{detailNodeId})
            """)
    int createArticleForDetailNode(@Param("detailNodeId") Long detailNodeId);

    @Select("""
            select ar.id, ar.content, ar.created_at, ar.updated_at
            from articles ar
            join detail_nodes de on de.id = ar.detail_node_id
            join directory_nodes dir on dir.id = de.directory_node_id
            where ar.detail_node_id = #{detailNodeId}
                  and dir.user_id = #{userId}
                  and dir.deleted_at is null
            """)
    Optional<FetchArticleRow> getArticleByUserIdAndDetailNodeId(@Param("userId") Long userId, @Param("detailNodeId") Long detailNodeId);

    @Select("""
            select
                ar.id,
                ar.content,
                ns.id as note_share_id,
                dir.id as directory_node_id,
                ar.created_at,
                ar.updated_at
            from articles ar
            join detail_nodes de on de.id = ar.detail_node_id
            join directory_nodes dir on dir.id = de.directory_node_id
            join note_shares ns on ns.directory_node_id = dir.id
            where ar.detail_node_id = #{detailNodeId}
                and ns.share_token = #{shareToken}
                and ns.status = 'ACTIVE'
                and dir.deleted_at is null
            """)
    Optional<GetNoteShareArticleRow> getNoteShareArticle(@Param("detailNodeId") Long detailNodeId, @Param("shareToken") String shareToken);

    @Update("""
            update articles ar
            join detail_nodes de on de.id = ar.detail_node_id
            join directory_nodes dir on dir.id = de.directory_node_id
            set
                ar.content = #{content}
            where ar.detail_node_id = #{detailNodeId}
                and dir.user_id = #{userId}
                and dir.deleted_at is null
            """)
    int updateArticleByDetailNodeId(@Param("userId") Long userId, @Param("detailNodeId") Long detailNodeId, @Param("content") String content);
}
