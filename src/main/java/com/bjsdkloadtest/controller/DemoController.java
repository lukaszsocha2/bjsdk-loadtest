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

    private final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private static final String JWT_CONFIG_PATH = "";
    private final BoxDeveloperEditionAPIConnection api;

    public DemoController() throws IOException {
        Reader reader = new FileReader(JWT_CONFIG_PATH);
        BoxConfig boxConfig = BoxConfig.readFrom(reader);
        this.api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig);
    }

    @RequestMapping("/loadtest")
    @ResponseBody
    public String runScenario() throws IOException {
        BoxFolder parentFolder = new BoxFolder(api, "0");

        BoxFolder.Info childFolderInfo = parentFolder.createFolder(UUID.randomUUID().toString());
        logger.info(String.format("Created folder %s with ID: %s", childFolderInfo.getName(), childFolderInfo.getID()));

        BoxFolder folder = childFolderInfo.getResource();

        for(int i=0; i<10; i++) {
            FileInputStream stream;
            try {
                File resource = new ClassPathResource("file.pdf").getFile();
                stream = new FileInputStream(resource.getPath());
            } catch (FileNotFoundException e) {
                continue;
            }

            BoxFile.Info file= folder.uploadFile(stream, UUID.randomUUID().toString());
            logger.info(String.format("Uploaded file %s with ID: %s", file.getName(), file.getID()));
        }

        for (BoxItem.Info itemInfo : folder.getChildren()) {
            BoxFile file = new BoxFile(api, itemInfo.getID());
            OutputStream stream = new ByteArrayOutputStream();
            file.download(stream);
            stream.close();
            logger.info(String.format("Downloaded file %s with ID: %s", itemInfo.getName(), itemInfo.getID()));

            file.delete();
            logger.info(String.format("Deleted file %s with ID: %s", itemInfo.getName(), itemInfo.getID()));
        }

        folder.delete(true);
        logger.info(String.format("Deleted folder %s with ID: %s", childFolderInfo.getName(), childFolderInfo.getID()));

        return "SUCCESS";
    }
}