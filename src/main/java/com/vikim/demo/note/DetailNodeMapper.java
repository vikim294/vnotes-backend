package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface DetailNodeMapper {


    @Select("""
            select id, parent_id, directory_node_id, label, x, y, created_at, updated_at
            from detail_nodes
            where directory_node_id = #{directoryNodeId} and deleted_at is null
    """)
    List<FetchDetailNodesResponse> fetchDetailNodesByDirectoryNodeId(@Param("directoryNodeId") Long directoryNodeId);


    int countActiveDetailNodesByIds(@Param("directoryNodeId") Long directoryNodeId, @Param("ids") Set<Long> ids);

    @Insert("""
            insert into detail_nodes(directory_node_id, parent_id, label, x, y)
            values(#{directoryNodeId} ,#{parentId}, #{label}, #{x}, #{y})
    """)
    @Options(
            useGeneratedKeys = true,
            keyProperty = "id"
    )
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
}
