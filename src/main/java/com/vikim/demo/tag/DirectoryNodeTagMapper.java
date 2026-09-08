package com.vikim.demo.tag;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface DirectoryNodeTagMapper {
    // todo 能不能改成直接覆盖新增，而不是纯新增？

    // 为指定目录节点 添加记录
    int batchCreateDirectoryNodeTags(@Param("directoryNodeId") Long directoryNodeId, @Param("tagIds") Set<Long> tagIds);

    // 删除指定目录节点有关的所有记录（如果不开放为接口的话，无需先校验用户吧？）
    int batchDeleteDirectoryNodeTags(Long directoryNodeId);

    // 根据目录节点找标签（需要提前校验 用户有该节点）
    List<TagRow> getTagsByDirectoryNodeId(Long directoryNodeId);
}
