package com.fh.controller;

import com.fh.entity.ResultVo;
import com.fh.service.ISolrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class SolrController {

    @Autowired
    private ISolrService solrService;

    // 获取全文检索的信息
    @RequestMapping("search")
    @ResponseBody
    public ResultVo search(String content, int current, int limit) throws Exception {
        return solrService.searchContent(content, current, limit);
    }

    @RequestMapping(value = "upload",method = RequestMethod.POST)
    @ResponseBody
    public ResultVo upload(@RequestParam(value = "file",required = false) MultipartFile multipartFile) throws Exception {
        return solrService.upload(multipartFile);
    }
}
