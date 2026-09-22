package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传接口")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}", file.getOriginalFilename());
        try {
            // 获取原始文件名
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                return Result.error("文件名不能为空");
            }
            // 截取原始文件名的后缀
            String extension = "";
            int dotIndex = fileName.lastIndexOf(".");
            if (dotIndex != -1) {
                extension = fileName.substring(dotIndex);
            }
            // 生成新的文件名
            String newFileName = UUID.randomUUID().toString() + extension;

            String filePath = aliOssUtil.upload(file.getBytes(), newFileName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
