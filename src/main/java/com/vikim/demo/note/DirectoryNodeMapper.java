package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
            select dn.id, dn.parent_id, dn.label, dn.x, dn.y, dn.node_type, coalesce(ns.status = 'ACTIVE', false) as shared, dn.created_at, dn.updated_at
            from directory_nodes dn
            left join note_shares ns
                on dn.id = ns.directory_node_id
            where dn.user_id = #{user_id} and dn.deleted_at is null
    """)
    List<DirectoryNodeRow> fetchAllDirectoryNodes(@Param("user_id") Long userId);

    List<Long> searchDirectoryNodes(SearchDirectoryNodeMapperParam param);

    int countDirectoryNodes(SearchDirectoryNodeMapperParam param);

    List<GetAncestorDirectoryNodesByIdsResponse> getAncestorDirectoryNodesByIds(@Param("ids") List<Long> ids);

    List<DirectoryNodeWithTagRow> getDirectoryNodesByIds(@Param("ids") List<Long> ids);

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

    List<SearchDirectoryNodesInTrashBinRow> searchDirectoryNodesInTrashBin(SearchDirectoryNodeInTrashBinMapperParam param);

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

    @Select("""
            select count(*)
            from directory_nodes
            where id = #{id} and
                node_type = 'DETAIL'
            """)
    int isWithDetail(Long id);

    // 分享（纯新增）
    @Insert("""
            insert into note_shares(directory_node_id, share_token, status)
            values(#{directoryNodeId}, #{shareToken}, 'ACTIVE')
            """)
    int createNoteShare(@Param("directoryNodeId") Long directoryNodeId, @Param("shareToken") String shareToken);

    // 分享（更新）
    @Update("""
            update note_shares
            set status = 'ACTIVE',
                share_token = #{shareToken}
            where status = 'REVOKED'
                and directory_node_id = #{directoryNodeId}
            """)
    int activateNoteShare(@Param("directoryNodeId") Long directoryNodeId, @Param("shareToken") String shareToken);

    // 取消分享
    @Update("""
            update note_shares
            set status = 'REVOKED'
            where status = 'ACTIVE'
                and directory_node_id = #{directoryNodeId}
            """)
    int cancelNoteShare(@Param("directoryNodeId") Long directoryNodeId);

    int batchCancelNoteShare(@Param("directoryNodeIds") Set<Long> directoryNodeIds);

    @Select("""
            select id, directory_node_id, share_token, status, created_at, updated_at
            from note_shares
            where directory_node_id = #{directoryNodeId}
            """)
    Optional<NoteShareRow> findNoteShareByDirectoryNodeId(@Param("directoryNodeId") Long directoryNodeId);

    @Select("""
            select id, directory_node_id, share_token, status, created_at, updated_at
            from note_shares
            where share_token = #{shareToken}
                and status = 'ACTIVE'
            """)
    Optional<NoteShareRow> findNoteShareByShareToken(@Param("shareToken") String shareToken);

    // 新增访问记录
    @Insert("""
            insert into note_share_access_logs(note_share_id, target, target_id, visitor_id)
            values(#{noteShareId}, #{target}, #{targetId}, #{visitorId})
            """)
    int createNoteShareAccessLog(@Param("noteShareId") Long noteShareId, @Param("target") NoteShareTarget target, @Param("targetId") Long targetId, @Param("visitorId") String visitorId);

    // 获取访问记录（cursor 分页）
    List<NoteShareAccessLogRow> fetchNoteShareAccessLogsByNoteShareId(@Param("noteShareId") Long noteShareId, @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt, @Param("cursorId") Long cursorId, @Param("limit") int limit);

    // 校验用户有该 note share 记录
    @Select("""
            select exists(
                select 1
                from directory_nodes dn
                join note_shares ns
                    on dn.id = ns.directory_node_id
                where dn.user_id = #{userId}
                    and ns.id = #{noteShareId}
            )
            """)
    boolean hasUserSharedNote(@Param("userId") Long userId, @Param("noteShareId") Long noteShareId);

    @Select("""
            select id, label, node_type, version
            from directory_nodes
            where id = #{id}
                and user_id = #{userId}
                and node_type = 'DETAIL'
                and deleted_at is null
            """)
    Optional<GetDirectoryNodeByIdResponse> getDirectoryNodeById(@Param("userId") Long userId, @Param("id") Long id);

    @Insert("""
            insert into note_search_history(user_id, keyword)
            values(#{userId}, #{keyword})
            on duplicate key update updated_at = current_timestamp(6)
            """)
    int createOrUpdateUserNoteSearchHistoryRecord(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Select("""
            select id, keyword
            from note_search_history
            where user_id = #{userId}
            order by updated_at desc, id desc
            limit 7
            """)
    List<NoteSearchHistoryResponse> getUserNoteSearchHistory(@Param("userId") Long userId);

    @Update("""
            update directory_nodes
            set version = version + 1
            where user_id = #{userId}
                and id = #{id}
                and version = #{version}
                and node_type = 'DETAIL'
                and deleted_at is null
            """)
    int updateVersion(@Param("userId") Long userId, @Param("id") Long id, @Param("version") Long version);

    @Update("""
            update directory_nodes
            set version = version + 1
            where user_id = #{userId}
                and node_type = 'DETAIL'
                and deleted_at is null
                and (
                    (
                        id = #{oldDirectoryNodeId}
                        and version = #{sourceVersion}
                    )
                    or id = #{destDirectoryNodeId}
                )
            """)
    int updateVersionsInNodeMove(@Param("userId") Long userId, @Param("oldDirectoryNodeId") Long oldDirectoryNodeId, @Param("sourceVersion") Long sourceVersion, @Param("destDirectoryNodeId") Long destDirectoryNodeId);
}
