package org.example.ai_api.Controller;

import org.example.ai_api.Bean.Entity.UserPermission;
import org.example.ai_api.Bean.Model.ResultData;
import org.example.ai_api.Service.UserPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户权限管理接口
 *
 * @author 10353965
 */
@RestController
@RequestMapping("/UserPermission")
public class UserPermissionController {
    @Autowired
    UserPermissionService userPermissionService;

    /**
     * 根据用户id获取用户权限
     *
     * @param userId 用户id
     * @return 用户权限
     */
    @PostMapping("/getUserPermissionByUserId")
    public ResultData<List<UserPermission>> getUserPermission(@RequestParam("userId") String userId) {
        List<UserPermission> permissions = userPermissionService.getUserPermissionListByUserId(userId);
        return ResultData.success("获取成功", permissions);
    }

    /**
     * 保存用户权限
     *
     * @param userPermissions 用户权限
     * @return 用户权限
     */
    @PostMapping("/saveUserPermission")
    public ResultData<List<UserPermission>> saveUserPermission(@RequestBody List<UserPermission> userPermissions) {
        List<UserPermission> permissions = userPermissionService.saveUserPermission(userPermissions);
        return ResultData.success("保存成功", permissions);
    }

    /**
     * 删除用户权限
     *
     * @param userId 用户id
     * @param target 领域
     * @return 删除结果
     */
    @PostMapping("/deleteUserPermission")
    public ResultData<String> deleteUserPermission(@RequestParam("userId") String userId, @RequestParam("target") String target) {
        userPermissionService.deleteUserPermission(userId, target);
        return ResultData.success("删除成功");
    }
}
