package com.fh.mapper;

import com.fh.entity.FileDetail;
import org.apache.ibatis.annotations.Param;

public interface SolrMapper {

    FileDetail getFileByName(String name);

    void insertFile(@Param("id") String id, @Param("name") String name);
}
