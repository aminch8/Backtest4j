package com.auctus.core.domains;


import com.auctus.core.utils.NumUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ta4j.core.num.Num;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private Num size= NumUtil.getNum(0);
    private Num averageEntryPrice=NumUtil.getNum(0);

    public boolean isNotEmpty(){
        return !size.isZero();
    }
    public boolean isShort(){ return size.isNegative();}
    public boolean isLong(){ return size.isPositive();}
}
