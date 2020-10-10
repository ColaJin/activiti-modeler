package com.flying.cattle.activiti.modeler.controller;

import com.flying.cattle.activiti.entity.FormData;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class ActivitiTaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 根据用户id查询个人任务列表
     *
     * @param modelMap
     * @param userId
     * @return
     */
    @RequestMapping("/queryTask")
    public String findTasksForSL(ModelMap modelMap, String userId) {
        String assignee = userId;
        List<Task> tasks = taskService.createTaskQuery()//条件
                .taskAssignee(assignee)//根据任务办理人查询任务
                //排序
                .orderByTaskCreateTime().desc()
                //结果集
                .list();
        modelMap.addAttribute("tasks",tasks);
        modelMap.addAttribute("userId",assignee);
        return "model/taskList";
    }

    /**
     *
     * @param formData
     * @param modelMap
     * @return
     */
    @RequestMapping("/form")
    public String form(FormData formData, ModelMap modelMap){
        List<Task> resultTask = taskService.createTaskQuery().taskId(formData.getId()).list();
        Task task=null;

        if (!CollectionUtils.isEmpty(resultTask)){
            task= resultTask.get(0);
        }
        modelMap.addAttribute("data",formData);
        modelMap.addAttribute("task",task);
        if(StringUtils.isNotEmpty(task.getFormKey())){
            return "activitiForm/"+task.getFormKey();
        }
        return "model/form";
    }

    //受理员受理数据
    @RequestMapping("/completeTaskSl")
    public String completeTasksForSL(ModelMap modelMap,FormData formData) {
        taskService.setAssignee(formData.getId(),formData.getUserId());
        taskService.complete(formData.getId());
        return findTasksForSL(modelMap,formData.getUserId());
    }
}
