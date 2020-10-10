package com.flying.cattle.activiti.modeler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flying.cattle.activiti.modeler.editor.model.ModelSaveRestResource;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Created by liuruijie on 2017/2/21.
 * 模型管理
 */
@Controller
@RequestMapping("/models")
public class ModelerController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelSaveRestResource.class);

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RepositoryService repositoryService;

    /**
     * 新建一个空模型，然后页面展示绘制流程图
     *
     * @param request
     * @param response
     * @throws UnsupportedEncodingException
     */
    @RequestMapping("/create")
    public void newModel(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException {
        try {
            //初始化一个空模型
            Model model = repositoryService.newModel();

            //设置一些默认信息
            String name = "new-process";
            String description = "";
            int revision = 1;
            String key = "process";

            ObjectNode modelNode = objectMapper.createObjectNode();
            modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

            model.setName(name);
            model.setKey(key);
            model.setMetaInfo(modelNode.toString());

            repositoryService.saveModel(model);
            String id = model.getId();

            //完善ModelEditorSource
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace",
                    "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));

            response.sendRedirect(request.getContextPath() + "/static/modeler.html?modelId=" + id);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("模型创建失败！");
        }

    }

    /**
     * 编辑保存和关闭后都会调用该接口，然后展示所有模型信息
     *
     * @param model
     * @param request
     * @return
     */
    @RequestMapping("/modelList")
    public String modelList(org.springframework.ui.Model model, HttpServletRequest request) {
        LOGGER.info("-------------列表页-------------");
        List<Model> models = repositoryService.createModelQuery().orderByCreateTime().desc().list();
        model.addAttribute("models", models);
        String info = request.getParameter("info");
        if (StringUtils.isNotEmpty(info)) {
            model.addAttribute("info", info);
        }
        return "model/list";
    }

    /**
     * 模型发布，返回String，注意@ResponseBody不可以取消
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/deployment/{id}", produces = "application/json;charset=utf-8")
    public @ResponseBody String deploy(@PathVariable("id") String id) throws Exception {

        //获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return "模型数据为空，请先设计流程并成功保存，再进行发布。";
        }

        JsonNode modelNode = new ObjectMapper().readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if (model.getProcesses().size() == 0) {
            return "数据模型不符要求，请至少设计一条主线流程。";
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        //发布流程
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(bpmnBytes, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
        return "流程发布成功";
    }

    /**
     * 根据模型id删除模型
     */
    @RequestMapping(value = "/delete/{id}", produces = "application/json;charset=utf-8")
    public @ResponseBody
    String deleteModel(@PathVariable("id") String id) {
        repositoryService.deleteModel(id);
        return "删除成功！";
    }

    @RequestMapping("/upLoadModel")
    public void uplaodModel(MultipartFile excelFile) throws Exception {
        //首先判断是不是空的文件
        if (!excelFile.isEmpty()) {
            //对文文件的全名进行截取然后在后缀名进行删选。
            int begin = excelFile.getOriginalFilename().indexOf(".");
            int last = excelFile.getOriginalFilename().length();
            String a = excelFile.getOriginalFilename().substring(begin, last);
            if (!a.endsWith(".zip")) {
                throw new RuntimeException("文件类型不是zip文件");
            }else {

                InputStream is = excelFile.getInputStream();

                ZipInputStream zipInputStream = new ZipInputStream(is);

                Deployment deployment = repositoryService.createDeployment().name(excelFile.getName())
                        .addZipInputStream(zipInputStream)//添加流程图的流
                        .deploy();//确定部署
                if (deployment == null) {
                    throw new RuntimeException("上传的文件流程部署失败");
                }
            }
        }
    }

}
