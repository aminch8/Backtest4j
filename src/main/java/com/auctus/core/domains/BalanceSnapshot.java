package com.auctus.core.domains;

import com.auctus.core.utils.NumUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ta4j.core.num.Num;

import java.time.ZonedDateTime;

@Getter
public class BalanceSnapshot {
    private ZonedDateTime time;
    private Num balanceUPNL= NumUtil.getNum(0);
    private Num balanceRPNL=NumUtil.getNum(0);



    public static BalanceSnapshot createSnapshot(ZonedDateTime time,Num balanceUPNL,Num balanceRPNL){
        BalanceSnapshot balanceSnapshot = new BalanceSnapshot();
        balanceSnapshot.time=time;
        balanceSnapshot.balanceRPNL=balanceRPNL;
        balanceSnapshot.balanceUPNL=balanceUPNL;
        return balanceSnapshot;
    }

    private BalanceSnapshot(){

    }

}
