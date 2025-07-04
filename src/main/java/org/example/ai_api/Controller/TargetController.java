package org.example.ai_api.Controller;

import org.example.ai_api.Bean.Entity.Target;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Service.TargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Target")
public class TargetController {
    @Autowired
    private TargetService targetService;

    @RequestMapping("/save")
    public ResultData<Target> save(@RequestBody Target target) {
        return ResultData.success("保存成功", targetService.save(target));
    }

    @RequestMapping("/findAll")
    public ResultData<List<Target>> findAll() {
        return ResultData.success("查询成功", targetService.findAll());
    }

    @RequestMapping("/delete")
    public ResultData<Void> delete(@RequestBody Target target) {
        targetService.delete(target);
        return ResultData.success("删除成功");
    }

}
