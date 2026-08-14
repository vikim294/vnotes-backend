package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface DirectoryNodeMapper {

    @Insert("""
            insert into directory_nodes(user_id, parent_id, label, x, y, node_type)
            values(#{userId} ,#{parentId}, #{label}, #{x}, #{y}, #{nodeType})
    """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "id"
    )
    int createDirectoryNode(CreateDirectoryNodeMapperParam param);

    @Select("""
            select id, parent_id, label, x, y, node_type, created_at, updated_at
            from directory_nodes dn
            where dn.user_id = #{user_id} and dn.deleted_at is null
    """)
    List<FetchDirectoryNodesResponse> fetchAllDirectoryNodes(@Param("user_id") Long userId);

    List<FetchDirectoryNodesResponse> searchDirectoryNodes(SearchDirectoryNodeMapperParam param);

    int countDirectoryNodes(SearchDirectoryNodeMapperParam param);

    @Select("""
        select exists(
            select 1
            from directory_nodes
            where user_id = #{userId} and id = #{id} and node_type = #{nodeType} and deleted_at is null
        )
    """)
    boolean hasDirectoryNode(@Param("userId") Long userId, @Param("id") Long id, @Param("nodeType") NodeType nodeType);

    int countActiveDirectoryNodesByIds(@Param("userId") Long userId, @Param("ids") Set<Long> ids);

    @Update("""
        with recursive subtree (id) as (
            select id
            from directory_nodes
            where id = #{id}
              and user_id = #{userId}
              and deleted_at is null

            union all

            select child.id
            from directory_nodes child
            join subtree parent
              on child.parent_id = parent.id
            where child.user_id = #{userId}
        )
        update directory_nodes node
        join subtree
          on subtree.id = node.id
        set node.deleted_at = current_timestamp(6)
        where node.deleted_at is null
        """)
    int deleteDirectoryNode(
            @Param("id") Long id,
            @Param("userId") Long userId
    );

    @Update("""
        update directory_nodes
        set parent_id = #{parentId}
        where id = #{id} and user_id = #{userId} and deleted_at is null
    """)
    int updateDirectoryNodeParentId(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("parentId") Long parentId
    );

    int updateDirectoryNode(UpdateDirectoryNodeMapperParam param);

    int batchDeleteDirectoryNodes(
            @Param("userId") Long userId,
            @Param("ids") Set<Long> ids
    );

    @Select("""
            select id as sourceId, parent_id as sourceParentId, label, x, y, node_type, created_at, updated_at
            from directory_nodes
            where user_id = #{user_id} and deleted_at is null
    """)
    List<ExportDirectoryNodeVO> getExportDirectoryNodes(@Param("user_id") Long userId);

    @Insert("""
            insert into directory_nodes(user_id, parent_id, label, x, y, node_type, created_at, updated_at)
            values(#{userId} ,#{parentId}, #{label}, #{x}, #{y}, #{nodeType}, #{createdAt}, #{updatedAt})
    """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "id"
    )
    int createDirectoryNodeFromImport(CreateDirectoryNodeFromImportMapperParam param);
}
