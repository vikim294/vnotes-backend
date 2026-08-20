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

    boolean hasDirectoryNode(@Param("userId") Long userId, @Param("id") Long id, @Param("nodeType") NodeType nodeType);

    int countActiveDirectoryNodesByIds(@Param("userId") Long userId, @Param("ids") Set<Long> ids);

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

    @Update("""
        update directory_nodes
        set original_parent_id = #{originalParentId}
        where id = #{id} and user_id = #{userId} and deleted_at is null
    """)
    int updateDirectoryNodeOriginalParentId(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("originalParentId") Long originalParentId
    );

    @Update("""
        update directory_nodes
        set
            parent_id = #{parentId},
            original_parent_id = null
        where id = #{id} and user_id = #{userId} and deleted_at is null
    """)
    int restoreDirectoryNodeParent(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("parentId") Long parentId
    );

    int updateDirectoryNode(UpdateDirectoryNodeMapperParam param);

    int batchDeleteDirectoryNodeSubTree(
            @Param("userId") Long userId,
            @Param("id") Long id
    );

    int batchRestoreDirectoryNodeSubTree(
            @Param("userId") Long userId,
            @Param("id") Long id
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

    List<SearchDirectoryNodesInTrashBinResponse> searchDirectoryNodesInTrashBin(SearchDirectoryNodeInTrashBinMapperParam param);

    int countDirectoryNodesInTrashBin(SearchDirectoryNodeInTrashBinMapperParam param);

    List<FindDeletedRootNodesByIdsResponse> findDeletedRootNodesByIds(@Param("userId") Long userId, @Param("ids") Set<Long> ids);

    @Delete("""
            delete from directory_nodes
            where user_id = #{userId} and
                deleted_at is not null and
                id = #{id}
    """)
    int deleteRootNodeFromTrashBin(@Param("userId") Long userId, @Param("id") Long id);

    @Select("""
             select id
                from directory_nodes
                where user_id = #{userId} and
                    deleted_at is not null and
                    deleted_root_id = #{id} and
                    node_type = 'DETAIL'
            """)
    List<Long> findDirectoryNodesWithDetailByRootId(@Param("userId") Long userId, @Param("id") Long id);

    @Select("""
             select user_id, id as deleted_root_id
                from directory_nodes
                where deleted_at is not null and
                    deleted_root_id = id and
                    deleted_at < date_sub(now(), interval 30 day)
            """)
    List<FindAllUsersAndDeletedRootNotesVO> findAllUsersAndDeletedRootNotes();
}
