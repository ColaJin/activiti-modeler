package com.flying.cattle.activiti.modeler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flying.cattle.activiti.utils.ActivitiUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.flying.cattle.activiti.modeler.controller.ModelerController.LOGGER;

@Controller
@RequestMapping("/processes")
public class ActivitiProcessController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private ProcessEngineConfigurationImpl processEngineConfiguration;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 每次更改流程图需要重新发布，然后启动流程(只有开始结点不创建流程实例)，获取模型页面的部署Id，查询表act_re_procdef获取流程定义id，根据流程定义id启动流程
     *
     * @param deploymentId
     * @return
     */
    @RequestMapping(value = "/start/{deploymentId}", produces = "application/json;charset=utf-8")
    public @ResponseBody
    String startProcess(@PathVariable("deploymentId") String deploymentId) {
        try {
            /*System.out.println(key + "key");
            String processDfinationKey = key;
            //根据流程部署关键字启动流程实例，key有空值的情况(流程关键字未编辑)
            runtimeService.startProcessInstanceByKey(processDfinationKey);*/
            //根据流程部署id查询表act_re_procdef获取流程定义id，根据流程定义id启动流程实例
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deploymentId)
                    .singleResult();
            runtimeService.startProcessInstanceById(processDefinition.getId());

        } catch (Exception e) {
            e.printStackTrace();
            return "流程启动失败！";
        }
        return "流程启动成功";
    }

    //生成流程图（高亮）---232501
    @RequestMapping("/queryProHighLighted")
    public @ResponseBody
    String queryProHighLighted(String proInsId, HttpServletResponse response) throws Exception {
        //获取历史流程实例
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(proInsId).singleResult();
        //获取流程图

        BpmnModel bi = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(proInsId).list();
        //高亮环节id集合
        List<String> highLightedActivitis = new ArrayList<String>();

        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }
        ProcessDiagramGenerator processDiagramGenerator = new DefaultProcessDiagramGenerator();
        /*InputStream generateDiagram = processDiagramGenerator.generateDiagram(bi, highLightedActivitis);
        IOUtils.copy(generateDiagram, new FileOutputStream(new File("d:/test2.png")));

        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] bpmnBytes = new byte[1024]; //buff用于存放循环读取的临时数据
        int rc = 0;
        while ((rc = generateDiagram.read(bpmnBytes, 0, 100)) > 0) {
            swapStream.write(bpmnBytes, 0, rc);
        }
        byte[] in_b = swapStream.toByteArray(); //in_b为转换之后的结果
        BASE64Encoder encoder = new BASE64Encoder();
        String png_base64 = encoder.encodeBuffer(in_b);//转换成base64串
        png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
        swapStream.close();
        generateDiagram.close();*/
        BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] bpmnBytes = xmlConverter.convertToXML(bi);
        ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
        IOUtils.copy(in, new FileOutputStream(new File("d:/test2.svg")));
        int rc = 0;
        while ((rc = in.read(bpmnBytes, 0, 100)) > 0) {
            swapStream.write(bpmnBytes, 0, rc);
        }
        byte[] in_b = swapStream.toByteArray(); //in_b为转换之后的结果
        BASE64Encoder encoder = new BASE64Encoder();
        String png_base64 = encoder.encodeBuffer(in_b);//转换成base64串
        png_base64 = png_base64.replaceAll("\n", "").replaceAll("\r", "");//删除 \r\n
        swapStream.close();
        in.close();
        return png_base64;
    }

    /**
     * <p>查看当前流程图</p>
     *
     * @param response void 响应
     * @author FRH
     * @time 2018年12月10日上午11:14:12
     * @version 1.0
     */
    @ResponseBody
    @RequestMapping(value = "/showImg")
    public void showImg(HttpServletResponse response) throws IOException {

        /*
         *  获取流程实例
         */
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId("092b0ab7-e907-11ea-a70a-d0c637ab19a3").singleResult();

        // 根据流程对象获取流程对象模型
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());


        /*
         *  查看已执行的节点集合
         *  获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
         */
        // 构造历史流程查询
        HistoricActivityInstanceQuery historyInstanceQuery = historyService.createHistoricActivityInstanceQuery().processInstanceId("092b0ab7-e907-11ea-a70a-d0c637ab19a3");
        // 查询历史节点
        List<HistoricActivityInstance> historicActivityInstanceList = historyInstanceQuery.orderByHistoricActivityInstanceStartTime().asc().list();
        // 已执行的节点ID集合(将historicActivityInstanceList中元素的activityId字段取出封装到executedActivityIdList)
        List<String> executedActivityIdList = historicActivityInstanceList.stream().map(item -> item.getActivityId()).collect(Collectors.toList());

        /*
         *  获取流程走过的线
         */
        // 获取流程定义
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService).getDeployedProcessDefinition(processInstance.getProcessDefinitionId());
        List<String> flowIds = ActivitiUtils.getHighLightedFlows(bpmnModel, processDefinition, historicActivityInstanceList);


        /*
         * 输出图像，并设置高亮
         */
        outputImg(response, bpmnModel, flowIds, executedActivityIdList);
    }

    /**
     * <p>输出图像</p>
     *
     * @param response               响应实体
     * @param bpmnModel              图像对象
     * @param flowIds                已执行的线集合
     * @param executedActivityIdList void 已执行的节点ID集合
     * @author FRH
     * @time 2018年12月10日上午11:23:01
     * @version 1.0
     */
    private void outputImg(HttpServletResponse response, BpmnModel bpmnModel, List<String> flowIds, List<String> executedActivityIdList) throws IOException {
        InputStream imageStream = null;
        ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        try {
            imageStream = diagramGenerator.generateDiagram(bpmnModel, executedActivityIdList, flowIds, "宋体", "微软雅黑", "黑体", true, "png");
            BufferedImage bi = ImageIO.read(imageStream);
            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally { // 流关闭
            imageStream.close();
        }
    }

    /*
     * 查询流程图
     */
    public void viewPic() throws IOException {
        /**将生成图片放到文件夹下*/
        String deploymentId = "401";
        //获取图片资源名称
        List<String> list = repositoryService.getDeploymentResourceNames(deploymentId);
        //定义图片资源的名称
        String resourceName = "";
        if (list != null && list.size() > 0) {
            for (String name : list) {
                if (name.indexOf(".png") >= 0) {
                    resourceName = name;
                }
            }
        }


        //获取图片的输入流
        InputStream in = repositoryService.getResourceAsStream(deploymentId, resourceName);

        //将图片生成到D盘的目录下
        File file = new File("D:/" + resourceName);
        //将输入流的图片写到D盘下
        FileUtils.copyInputStreamToFile(in, file);
    }

    @RequestMapping(value = {"/model"})
    @ResponseBody
    public ObjectNode getEditorJson(HttpServletRequest request) {
        ObjectNode modelNode = null;
        String modelId = request.getParameter("modelId");
        Model model = repositoryService.getModel(modelId);
        if (model != null) {
            try {
                if (StringUtils.isNotEmpty(model.getMetaInfo())) {
                    modelNode = (ObjectNode)objectMapper.readTree(model.getMetaInfo());
                } else {
                    modelNode = objectMapper.createObjectNode();
                    modelNode.put("name", model.getName());
                }

                modelNode.put("modelId", model.getId());
                ObjectNode editorJsonNode = (ObjectNode)objectMapper.readTree(new String(repositoryService.getModelEditorSource(model.getId()), "utf-8"));
                modelNode.put("model", editorJsonNode);
            } catch (Exception var5) {
                LOGGER.error("Error creating model JSON", var5);
                throw new ActivitiException("Error creating model JSON", var5);
            }
        }

        return modelNode;
    }

    @RequestMapping("/model/test02")
    public void test02(Object obj) throws Exception {
        Model modelData = repositoryService.getModel("1d49e175-ebf4-11ea-9c23-d0c637ab19a3");
        ObjectNode modelNode = (ObjectNode) new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
        byte[] bpmnBytes = null;

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        bpmnBytes = new BpmnXMLConverter().convertToXML(model);

        String processName = modelData.getName() + ".bpmn";

        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName()).addString(processName, new String(bpmnBytes,"UTF-8"))
                .deploy();

    }

    @RequestMapping("/model/test03")
    public void genPic(String procId) throws Exception {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(procId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());
        List<String> activeIds = runtimeService.getActiveActivityIds(processInstance.getId());
        ProcessDiagramGenerator p = new DefaultProcessDiagramGenerator();
        InputStream is = p.generateDiagram(bpmnModel, activeIds);

        File file = new File("d:\\process.png");
        OutputStream os = new FileOutputStream(file);

        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

        os.close();
        is.close();
    }
}
