package org.scoula.finance.dto.wishlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.finance.dto.deposit.DepositListDto;
import org.scoula.finance.dto.fund.FundListDto;
import org.scoula.finance.dto.installment.InstallmentListDto;
import org.scoula.finance.dto.stock.StockListDto;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class WishListResponseDto {
    private List<DepositListDto> depositList;
    private List<InstallmentListDto> installmentList;
    private List<FundListDto> fundList;
    private List<StockListDto> stockList;
}
