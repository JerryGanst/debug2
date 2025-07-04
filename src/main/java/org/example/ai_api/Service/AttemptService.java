package org.example.ai_api.Service;

import org.example.ai_api.Bean.Entity.UserLoginAttempt;
import org.example.ai_api.Exception.NotAccessedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountLockedException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class AttemptService {
    @Autowired
    private MongoTemplate mongoTemplate;
    //最大尝试次数
    @Value("${MAX_ATTEMPTS}")
    private int MAX_ATTEMPTS;
    //锁定时间
    @Value("${LOCK_MINUTES}")
    private int LOCK_MINUTES;
    //记录保留天数
    @Value("${EXPIRE_DAYS}")
    private int EXPIRE_DAYS;
    //锁定后的记录保留时间
    @Value("${SAFETY_BUFFER}")
    private int SAFETY_BUFFER;

    /**
     * 检查用户是否被锁定
     * @param userId 用户id
     * @throws AccountLockedException 被锁定时抛出异常
     */
    public void isLocked(String userId) throws AccountLockedException {
        UserLoginAttempt attempt = mongoTemplate.findOne(
                Query.query(Criteria.where("userId").is(userId)),
                UserLoginAttempt.class
        );

        // 不存在记录 = 未锁定
        if (attempt == null) return;

        // 存在记录但未设置锁定 = 未锁定
        if (attempt.getLockUntil() == null) return;

        // 锁定已过期 = 清除记录并返回未锁定
        if (attempt.getLockUntil().before(new Date())) {
            mongoTemplate.remove(attempt);
            return;
        }
        // 格式化时间信息
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String lockTime = sdf.format(attempt.getLastAttemptTime());
        String unlockTime = sdf.format(attempt.getLockUntil());

        // 计算剩余解锁时间（分钟）
        long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(
                attempt.getLockUntil().getTime() - System.currentTimeMillis()
        );

        // 构建用户友好的错误提示
        String errorMsg = String.format(
                "您的账号因连续登录失败已被锁定。" +
                        "\n锁定时间: %s" +
                        "\n预计解锁时间: %s" +
                        "\n（约 %d 分钟后自动解锁）" +
                        "\n请勿频繁尝试，否则可能延长锁定时间",
                lockTime, unlockTime, remainingMinutes + 1
        );

        throw new AccountLockedException(errorMsg);
    }

    /**
     * 记录登录失败尝试
     * @param userId 用户id
     */
    public void recordFailedAttempt(String userId) {
        Query query = Query.query(Criteria.where("userId").is(userId));

        // 原子操作：增加失败计数
        UserLoginAttempt attempt = mongoTemplate.findAndModify(
                query,
                new Update()
                        .inc("count", 1)
                        .set("lastAttemptTime", new Date())
                        .set("expireAt", new Date(System.currentTimeMillis() +
                                TimeUnit.DAYS.toMillis(EXPIRE_DAYS))),
                new FindAndModifyOptions()
                        .returnNew(true)
                        .upsert(true),
                UserLoginAttempt.class
        );

        // 达到阈值时设置锁定
        if (attempt != null && attempt.getCount() >= MAX_ATTEMPTS) {
            Date lockUntil = new Date(System.currentTimeMillis() +
                    TimeUnit.MINUTES.toMillis(LOCK_MINUTES));
            // 安全过期时间 = max(锁定结束+1天, 当前+7天)
            long safeExpire = Math.max(
                    lockUntil.getTime() + TimeUnit.DAYS.toMillis(SAFETY_BUFFER),
                    System.currentTimeMillis() + TimeUnit.DAYS.toMillis(EXPIRE_DAYS)
            );
            mongoTemplate.updateFirst(
                    query,
                    new Update()
                            .set("lockUntil", lockUntil)
                            .set("expireAt", new Date(safeExpire)),
                    UserLoginAttempt.class
            );
        }
    }

    /**
     * 成功登陆后重置登录失败尝试
     * @param userId 用户id
     */
    public void resetFailedAttempts(String userId) {
        mongoTemplate.remove(
                Query.query(Criteria.where("userId").is(userId)),
                UserLoginAttempt.class
        );
    }
}
