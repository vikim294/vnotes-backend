package com.vikim.vnotes.tag;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface TagMapper {
    @Insert("""
            insert into tags(user_id, name)
            values(#{userId}, #{name})
            """)
    int createTag(@Param("userId") Long userId, @Param("name") String name);

    @Select("""
            select id, name, created_at
            from tags
            where user_id = #{userId}
            """)
    List<TagRow> fetchTagsByUserId(Long userId);

    @Update("""
            update tags
            set name = #{name}
            where id = #{id} and
                user_id = #{userId}
            """)
    int updateTagName(@Param("userId") Long userId, @Param("id") Long id, @Param("name") String name);

    @Delete("""
            delete from tags
            where id = #{id} and user_id = #{userId}
            """)
    int deleteTag(@Param("userId") Long userId, @Param("id") Long id);

    int checkUserHasTags(@Param("userId") Long userId, @Param("tagIds") Set<Long> tagIds);
}
