package org.scoula.ars.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.ars.domain.ArsVO;

@Mapper
public interface ArsMapper {
     void update(@Param("userId") Long userId,@Param("vo") ArsVO vo);
     ArsVO findById(@Param("userId")Long userId);
}
