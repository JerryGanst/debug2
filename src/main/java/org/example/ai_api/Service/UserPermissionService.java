package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.Target;
import org.example.ai_api.Bean.Entity.UserPermission;
import org.example.ai_api.Repository.TargetRepository;
import org.example.ai_api.Repository.UserPermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserPermissionService {
    @Autowired
    private UserPermissionRepository userPermissionRepository;
    @Autowired
    private TargetRepository targetRepository;

    public List<UserPermission> getUserPermissionListByUserId(String userId) {
        List<UserPermission> userPermissions = userPermissionRepository.findUserPermissionByUserId(userId);
        List<UserPermission> defaultPermission = getDefaultPermission(userId);
        Map<String, UserPermission> userPermissionMap = userPermissions.stream()
                .collect(Collectors.toMap(UserPermission::getTarget, userPermission -> userPermission));
        List<UserPermission> result = new ArrayList<>();
        if (userPermissionMap.isEmpty()) {
            return defaultPermission;
        }
        for (UserPermission userPermission : defaultPermission) {
            UserPermission perm = userPermissionMap.get(userPermission.getTarget());
            if (perm != null) {
                result.add(perm); // 使用已有权限
            } else {
                result.add(userPermission);
            }
        }
        return result;
    }

    public List<UserPermission> saveUserPermission(List<UserPermission> userPermissions) {
        return userPermissionRepository.saveAll(userPermissions);
    }

    public void deleteUserPermission(String userId, String target) {
        userPermissionRepository.removeUserPermissionByUserIdAndTarget(userId, target);
    }

    public UserPermission checkUserPermission(String userId, String target) {
        UserPermission userPermission = userPermissionRepository.findUserPermissionByUserIdAndTarget(userId, target);
        if (userPermission == null) {
            return new UserPermission(null, userId, target, false, false, false);
        } else {
            return userPermission;
        }
    }

    public List<UserPermission> getDefaultPermission(String userId) {
        List<Target> targets = targetRepository.findAll();
        return targets.stream()
                .map(target -> {
                    UserPermission permission = new UserPermission();
                    permission.setUserId(userId);
                    permission.setTarget(target.getTargetName());
                    permission.setDelete(target.isDelete());
                    permission.setUpload(target.isUpload());
                    permission.setRead(target.isRead());
                    return permission;
                })
                .collect(Collectors.toList());
    }
}
