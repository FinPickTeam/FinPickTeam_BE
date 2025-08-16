package org.scoula.ars.domain;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ArsVO {

    String userName;

    String phoneNum;

    LocalDate birthday;

}