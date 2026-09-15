package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper
public interface DetailNodeMapper {

    @Select("""
            select dn.id, dn.parent_id, dn.directory_node_id, dn.label, dn.image_url, dn.x, dn.y, dn.image_width, dn.image_height, dn.content_type, ar.id is not null as article, dn.mark_type, dn.ref_directory_node_id, dn.ref_detail_node_id, dn.created_at, dn.updated_at
            from detail_nodes dn
            left join articles ar on ar.detail_node_id = dn.id
            where directory_node_id = #{directoryNodeId}
    """)
    List<FetchDetailNodesRow> fetchDetailNodesByDirectoryNodeId(@Param("directoryNodeId") Long directoryNodeId);


    int countActiveDetailNodesByIds(@Param("directoryNodeId") Long directoryNodeId, @Param("ids") Set<Long> ids);

    int createDetailNode(CreateDetailNodeMapperParam param);

    @Update("""
        update detail_nodes
        set parent_id = #{parentId}
        where id = #{id} and directory_node_id = #{directoryNodeId}
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
            select dn.id, dn.parent_id, dn.directory_node_id, dn.label, dn.image_url, dn.x, dn.y, dn.image_width, dn.image_height, dn.content_type, ar.id is not null as article, dn.mark_type, dn.ref_directory_node_id, dn.ref_detail_node_id, dn.created_at, dn.updated_at
            from detail_nodes dn
            left join articles ar on ar.detail_node_id = dn.id
            where dn.id = #{id} and dn.directory_node_id = #{directoryNodeId}
    """)
    Optional<FetchDetailNodesRow> getDetailNodeById(@Param("id") Long id, @Param("directoryNodeId") Long directoryNodeId);

    List<ExportDetailNodeVO> getExportDetailNodesByDirectoryNodeIds(@Param("directoryNodeIds") List<Long> directoryNodeIds);

    int createDetailNodeFromImport(CreateDetailNodeFromImportMapperParam param);

    // 根据 ids 找出图片节点、后代图片节点
    List<ImageDetailNodeResponse> getImageNodesFromIds(
            @Param("directoryNodeId") Long directoryNodeId,
            @Param("ids") Set<Long> ids
    );

    int deleteDetailNodesOfDirectoryRootNodes(@Param("ids") List<Long> ids);

    List<FindImageNodesByDirectoryNodesWithDetailResponse> findImageNodesByDirectoryNodesWithDetail(@Param("ids") List<Long> ids);

    int moveDetailNode(@Param("detailNodeId") Long detailNodeId, @Param("oldDirectoryNodeId") Long oldDirectoryNodeId, @Param("destDirectoryNodeId") Long destDirectoryNodeId, @Param("offsetX") BigDecimal offsetX, @Param("offsetY") BigDecimal offsetY);

    boolean validateRefNodeForCreate(@Param("refType") DetailNodeContentType refType, @Param("refId") Long refId, @Param("directoryNodeId") Long directoryNodeId);
}
