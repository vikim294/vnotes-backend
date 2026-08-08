package com.vikim.demo.note;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DetailNodeMapper {


    @Select("""
            select id, parent_id, directory_node_id, label, x, y, created_at, updated_at
            from detail_nodes
            where directory_node_id = #{directoryNodeId} and deleted_at is null
    """)
    List<FetchDetailNodesResponse> fetchDetailNodesByDirectoryNodeId(@Param("directoryNodeId") Long directoryNodeId);


}
