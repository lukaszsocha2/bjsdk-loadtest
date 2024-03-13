package com.bjsdkloadtest.controller;

import com.box.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.util.UUID;


@Controller
public class DemoController {

    private static final String JWT_CONFIG_PATH = "";
    private final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private final BoxDeveloperEditionAPIConnection api;
    private final String testFileId;
    private final String testFolderId;

    public DemoController() throws IOException {
        Reader reader = new FileReader(JWT_CONFIG_PATH);
        BoxConfig boxConfig = BoxConfig.readFrom(reader);
        this.api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
        BoxLogger.defaultLogger().setLevelToAll();

        // Create test folder
        BoxFolder parentFolder = new BoxFolder(api, "0");
        BoxFolder.Info childFolderInfo = parentFolder.createFolder(UUID.randomUUID().toString());
        logger.info(String.format("Created test folder %s with ID: %s", childFolderInfo.getName(), childFolderInfo.getID()));
        this.testFolderId = childFolderInfo.getID();

        // Upload a test file
        this.testFileId = upload();
    }


    @RequestMapping("/download")
    @ResponseBody
    public String download() throws IOException {

        BoxFile file = new BoxFile(api, this.testFileId);
        OutputStream stream = new ByteArrayOutputStream();
        file.download(stream);
        stream.close();
        logger.info(String.format("Downloaded file with ID: %s", this.testFileId));
        return "SUCCESS";
    }


    @RequestMapping("/upload")
    @ResponseBody
    public String upload() throws IOException {

        BoxFolder folder = new BoxFolder(api, this.testFolderId);

        FileInputStream stream;
        try {
            File resource = new ClassPathResource("file.pdf").getFile();
            stream = new FileInputStream(resource.getPath());

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        BoxFile.Info fileInfo = folder.uploadFile(stream, UUID.randomUUID().toString());
        logger.info(String.format("Uploaded file %s with ID: %s", fileInfo.getName(), fileInfo.getID()));
        return fileInfo.getID();
    }

}
