package io.github.myacelw.mybatis.dynamic.core.service.entity;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DateBetween {
    LocalDate startDate;

    LocalDate endDate;
}
