package com.itheima.reggie.controller;

import com.itheima.reggie.commen.AliOSSUtils;
import com.itheima.reggie.commen.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;


@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {
    //动态路径
  @Value("${reggie.path}")
    private String basePath;
    /**
     * 文件上传
     *
     * @param file
     * @return
     */
@Autowired
private AliOSSUtils aliOSSUtils;

    //本地存储
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //file是一个临时文件,需要转存到指定位置,否则本次请求完成后临时文件会删除
        //获取原始文件名 (用字符串截取获取图片后缀 用uuid使图片名称唯一)
        String originalFilename = file.getOriginalFilename();//adb.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));//jpg
        String fileName = UUID.randomUUID().toString() + suffix;//uuid+jpg
        //创建一个目录对象
        File dir = new File(basePath);
        //判断当前目录是否存在
        if(!dir.exists()){
            //目录不存在,需要创建
            dir.mkdirs();
        }

        //将临时文件存储在服务器的磁盘目录下C:\img
        file.transferTo(new File(basePath + fileName));
        return R.success(fileName);
    }

    //云存储文件
//    @PostMapping("/upload")
//    public R<String> upload(MultipartFile file) throws IOException {
//        log.info("文件上上传:{}", file.getOriginalFilename());
//        String url = aliOSSUtils.upload(file);
//        log.info("文件上传完成,文件访问url{}", url);
//        return R.success("上传成功");
//    }

    /**
     * 图片回显
     * @param name
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        //输入流,通过输入流读取文件内容
        FileInputStream fis=new FileInputStream(new File(basePath+name));
        //输出流,通过输出流将文件写回浏览器,在浏览器展示图片
        ServletOutputStream outputStream = response.getOutputStream();

        response.setContentType("image/jpeg");

        int len;
        byte[]bytes=new byte[1024];
        while ((len=fis.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }
        outputStream.close();
        fis.close();
    }
}
