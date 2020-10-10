package com.flying.cattle.activiti.modeler.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flying.cattle.activiti.entity.FormData;
import com.flying.cattle.activiti.entity.Order;
import com.flying.cattle.activiti.entity.User;
import com.flying.cattle.activiti.modeler.editor.model.ModelSaveRestResource;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/all")
public class AllActivitiInterface {
    @Autowired
    RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    ObjectMapper objectMapper;

    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelSaveRestResource.class);

    /*********************************************模型相关开始*************************************/

    /**
     * 新建空模型
     */
    @ApiOperation(value = "新建空模型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "名称", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "description", value = "描述", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "key", value = "key", dataType = "string", paramType = "query"),
    })
    @RequestMapping("/create")
    public void create(
            @RequestParam("name") String name,
            @RequestParam("key") String key,
            @RequestParam(value = "description", required = false) String description,
            HttpServletRequest request, HttpServletResponse response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION,
                    org.apache.commons.lang3.StringUtils
                            .defaultString(description));
            Model newModel = repositoryService.newModel();
            newModel.setMetaInfo(modelObjectNode.toString());
            newModel.setName(name);
            newModel.setKey(key);
            repositoryService.saveModel(newModel);
            String id = newModel.getId();
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace",
                    "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(newModel.getId(), editorNode
                    .toString().getBytes("utf-8"));
            response.sendRedirect(request.getContextPath() + "/static/modeler.html?modelId=" + id);
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    /**
     * 编辑保存和关闭后都会调用该接口，然后展示所有模型信息
     *
     * @param model
     * @param request
     * @return
     */
    @ApiOperation(value = "编辑保存和关闭modeler，显示所有模型信息")
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
    @ApiOperation(value = "发布选中模型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模型id", dataType = "string", paramType = "query"),
    })
    @RequestMapping(value = "/deployment/{id}", produces = "application/json;charset=utf-8")
    public @ResponseBody
    String deploy(@PathVariable("id") String id) throws Exception {

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
    @ApiOperation(value = "删除选中模型")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模型id", dataType = "string", paramType = "query"),
    })
    @RequestMapping(value = "/delete/{id}", produces = "application/json;charset=utf-8")
    public @ResponseBody
    String deleteModel(@PathVariable("id") String id) {
        repositoryService.deleteModel(id);
        return "模型删除成功！";
    }

    /*********************************************模型相关结束*************************************/

    /*********************************************流程相关开始*************************************/
    /**
     * 每次更改流程图需要重新发布，然后启动流程(只有开始结点不创建流程实例)，获取模型页面的部署Id，查询表act_re_procdef获取流程定义id，根据流程定义id启动流程
     *
     * @param deploymentId
     * @return
     */
    @ApiOperation(value = "启动流程")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deploymentId", value = "选中模型的部署ID", dataType = "string", paramType = "query"),
    })
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

    /*********************************************流程相关结束*************************************/

    /*********************************************任务相关开始*************************************/
    /**
     * 根据用户id查询个人任务列表
     *
     * @param modelMap
     * @param userId
     * @return
     */
    @ApiOperation(value = "根据用户Id查询用户的任务")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "登录用户的Id", dataType = "string", paramType = "query"),
    })
    @RequestMapping("/queryTask")
    public String findTasksForSL(ModelMap modelMap, String userId) {
        String assignee = userId;
        List<Task> tasks = taskService.createTaskQuery()//条件
                .taskAssignee(assignee)//根据任务办理人查询任务
                //排序
                .orderByTaskCreateTime().desc()
                //结果集
                .list();
        modelMap.addAttribute("tasks", tasks);
        modelMap.addAttribute("userId", assignee);
        return "model/taskList";
    }

    /**
     * 根据用户任务的FormKey(指定申请是applyForm,审批是approveForm,否则是form)
     *
     * @param formData
     * @param modelMap
     * @return
     */
    @RequestMapping("/form")
    public String form(FormData formData, ModelMap modelMap) {
        List<Task> resultTask = taskService.createTaskQuery().taskId(formData.getId()).list();
        Task task = null;

        if (!CollectionUtils.isEmpty(resultTask)) {
            task = resultTask.get(0);
        }
        modelMap.addAttribute("data", formData);
        modelMap.addAttribute("task", task);
        if (StringUtils.isNotEmpty(task.getFormKey())) {
            return "activitiForm/" + task.getFormKey();
        }
        return "model/form";
    }

    /**
     * 执行任务
     *
     * @param modelMap
     * @param formData
     * @return
     */
    @RequestMapping("/completeTaskSl")
    public String completeTasksForSL(ModelMap modelMap, FormData formData) {
        taskService.setAssignee(formData.getId(), formData.getUserId());
        taskService.complete(formData.getId());
        return findTasksForSL(modelMap, formData.getUserId());
    }

    /*********************************************任务相关结束*************************************/
}
