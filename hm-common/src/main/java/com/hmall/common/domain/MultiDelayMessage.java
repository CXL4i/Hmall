package com.hmall.common.domain;

import lombok.Data;

import java.util.List;
@Data
public class MultiDelayMessage <T>{
    public T data;
    public List<Integer> delayTime;

    public MultiDelayMessage(T data, List<Integer> delayTime) {
        this.data = data;
        this.delayTime = delayTime;
    }
    public static <T> MultiDelayMessage<T> of(T data, Integer... delayTimes){
        return new MultiDelayMessage<>(data, List.of(delayTimes));
    }
    public Integer getNextDelayTime(){
        return delayTime.remove(0);
    }

    public boolean hasNextDelayTime(){
        return !delayTime.isEmpty();
    }
}
