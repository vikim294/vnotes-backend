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
    Optional<FetchArticleRow> getArticleByDetailNodeId(@Param("userId") Long userId, @Param("detailNodeId") Long detailNodeId);

    @Select("""
            select exists(
                select 1
                from directory_nodes dir
                join detail_nodes de on dir.id = de.directory_node_id
                join articles ar on ar.detail_node_id = de.id
                where de.id = #{detailNodeId} and dir.user_id = #{userId} and dir.deleted_at is null
            )
            """)
    boolean hasArticle(@Param("userId") Long userId, @Param("detailNodeId") Long detailNodeId);

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
