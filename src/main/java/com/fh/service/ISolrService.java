package com.fh.service;

import com.fh.entity.ResultVo;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ISolrService {
    ResultVo searchContent(String content, int current, int limit) throws IOException, SolrServerException, Exception;

    ResultVo upload(MultipartFile multipartFile) throws IOException, SolrServerException;
}
