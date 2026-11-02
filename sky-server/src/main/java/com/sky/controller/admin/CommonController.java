package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用的功能接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
@Api(tags = "通用功能相关接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 文件上传接口
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传接口")
    public Result<String> fileUpload(@RequestBody MultipartFile file) {
        log.info("文件上传：{}", file);

        try {
            // 获取文件的原始文件名
            String fileName = file.getOriginalFilename();

            // 截取原始文件的扩展名
            String extension = fileName.substring(fileName.lastIndexOf("."));
            String objectName = UUID.randomUUID() + extension;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e.getMessage());
        }

        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
