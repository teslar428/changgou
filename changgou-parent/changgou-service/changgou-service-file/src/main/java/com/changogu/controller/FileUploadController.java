package com.changogu.controller;

import com.changogu.file.FastDFSFile;
import com.changogu.util.FastDFSUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*****
 * @Author: www.itheima.com
 * @Description: com.changogu.controller
 ****/
@RestController
@RequestMapping(value = "/upload")
@CrossOrigin
public class FileUploadController {

    /****
     * 文件上传
     */
    @PostMapping
    public Result upload(@RequestParam(value = "file")MultipartFile file) throws Exception{
        if(1==1){
            return new Result(true,StatusCode.OK,"文件上传成功！","http://yun.itheima.com/Upload/Images/2019-08-02/5d4392ec4d1ed.jpg");
        }
        //封装文件信息
        FastDFSFile fastDFSFile = new FastDFSFile(
                file.getOriginalFilename(), //文件名字  1.jpg
                file.getBytes(),            //文件字节数组
                StringUtils.getFilenameExtension(file.getOriginalFilename())    //获取文件扩展名
        );

        //调用FastDFSUtil工具类将文件传入到FastDFS中
        String[] uploads = FastDFSUtil.upload(fastDFSFile);

        //拼接访问地址 url = http://127.0.0.1:8080/group1/M00/00/00/wKjThF0DBzaAP23MAAXz2mMp9oM26.jpeg
        //String url ="http://127.0.0.1:8080/"+uploads[0]+"/"+uploads[1];
        String url =FastDFSUtil.getTrackerInfo()+"/"+uploads[0]+"/"+uploads[1];
        return new Result(true, StatusCode.OK,"上传成功！",url);
    }

}
