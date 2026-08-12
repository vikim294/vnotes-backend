package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface DetailNodeMapper {

    @Select("""
            select id, parent_id, directory_node_id, label, image_url, x, y, image_width, image_height, content_type, mark_type, created_at, updated_at
            from detail_nodes
            where directory_node_id = #{directoryNodeId} and deleted_at is null
    """)
    List<FetchDetailNodesResponse> fetchDetailNodesByDirectoryNodeId(@Param("directoryNodeId") Long directoryNodeId);


    int countActiveDetailNodesByIds(@Param("directoryNodeId") Long directoryNodeId, @Param("ids") Set<Long> ids);

    int createDetailNode(CreateDetailNodeMapperParam param);

    @Update("""
        update detail_nodes
        set parent_id = #{parentId}
        where id = #{id} and directory_node_id = #{directoryNodeId} and deleted_at is null
    """)
    int updateDetailNodeParentId(
            @Param("id") Long id,
            @Param("directoryNodeId") Long directoryNodeId,
            @Param("parentId") Long parentId
    );

    int updateDetailNode(UpdateDetailNodeMapperParam param);

    int batchDeleteDetailNodes(
            @Param("directoryNodeId") Long directoryNodeId,
            @Param("ids") Set<Long> ids
    );

    @Select("""
            select id, parent_id, directory_node_id, label, image_url, x, y, image_width, image_height, content_type, mark_type, created_at, updated_at
            from detail_nodes
            where id = #{id} and directory_node_id = #{directoryNodeId} and deleted_at is null
    """)
    Optional<FetchDetailNodesResponse> getDetailNodeById(@Param("id") Long id, @Param("directoryNodeId") Long directoryNodeId);
}
