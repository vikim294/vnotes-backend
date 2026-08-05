package com.vikim.demo.note;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DirectoryNodeMapper {

    @Insert("""
            insert into directory_nodes(user_id, label, x, y)
            values(#{user_id},#{label},#{x},#{y})
            """)
    int createDirectoryNode(@Param("user_id") Long userId, @Param("label") String label, @Param("x") BigDecimal x, @Param("y") BigDecimal y);

    @Select("""
            select id, label, x, y, created_at, updated_at
            from directory_nodes dn
            where dn.user_id = #{user_id}
            """)
    List<FetchDirectoryNodesResponse> fetchAllDirectoryNodes(@Param("user_id") Long userId);
}
