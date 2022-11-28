package com.auctus.core.domains;

import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import lombok.Getter;
import org.ta4j.core.num.Num;

import java.lang.reflect.InvocationTargetException;


class Costs {

    CostType costType = CostType.ABSOLUTE;
    Num value = NumUtil.getNum(0);

}
