package com.auctus.core.domains;

import com.auctus.core.domains.enums.CostType;
import com.auctus.core.utils.NumUtil;
import org.ta4j.core.num.Num;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.function.Supplier;


class Costs {

    CostType costType = CostType.ABSOLUTE;
    Num value = NumUtil.getNum(0);

}
