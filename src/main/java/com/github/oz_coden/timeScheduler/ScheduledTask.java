package com.github.oz_coden.timeScheduler;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ScheduledTask {
    private final UUID id;
    private final String register;
    private final String target;
    private final ScheduleType type;
    private final long targetTime;
    private final String message;

    // 新しくスケジュールを作るとき用のコンストラクタ
    public ScheduledTask(String register, ScheduleType type, String target, @NotNull Long targetTime, String message) {
        this.id = UUID.randomUUID();
        this.register = register;
        this.target = target;
        this.type = type;
        this.targetTime = targetTime;
        this.message = message;
    }

    public ScheduledTask(UUID id, String register, String target, ScheduleType type, @NotNull Long targetTime, String message) {
        this.id = id;
        this.register = register;
        this.target = target;
        this.type = type;
        this.targetTime = targetTime;
        this.message = message;
    }

    public UUID getId() { return id; }
    public String getRegister() { return register; }
    public String getTarget() { return target; }
    public ScheduleType getType() { return type; }
    public long getTargetTime() { return targetTime; }
    public String getMessage() { return message; }
}