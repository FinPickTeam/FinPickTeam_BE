import sys
import os
import json
from pykrx import stock

output_path = "./data/stock/output/stock_return.json"
os.makedirs(os.path.dirname(output_path), exist_ok=True)

def fmt_signed(amount: int) -> str:
    if amount > 0:
        return f"+{amount:,}"
    elif amount < 0:
        return f"-{abs(amount):,}"
    return "0"

def main():
    if len(sys.argv) != 4:
        print("0", flush=True)
        return

    code, start, end = sys.argv[1], sys.argv[2], sys.argv[3]

    try:
        df = stock.get_market_ohlcv_by_date(start, end, code, freq='d')
        if df is None or df.empty or len(df) < 2:
            print("0", flush=True)
            return

        first_close = float(df["종가"].iloc[0])
        last_close  = float(df["종가"].iloc[-1])
        if first_close <= 0:
            print("0", flush=True)
            return

        cum_ret = (last_close / first_close) - 1.0
        pnl = round(1_000_000 * cum_ret)  # 100만원 기준 손익(원)

        # JSON 저장 (pnl만)
        with open(output_path, mode="w", encoding="utf-8") as f:
            json.dump({"pnl": fmt_signed(int(pnl))}, f, ensure_ascii=False)

        # 표준출력 (포맷 적용)
        print(fmt_signed(int(pnl)), flush=True)

    except Exception:
        print("0", flush=True)

if __name__ == "__main__":
    main()
