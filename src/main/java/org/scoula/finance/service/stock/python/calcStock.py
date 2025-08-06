import pandas as pd
import numpy as np
import json
import os
import sys
from datetime import datetime
from statsmodels.api import OLS, add_constant
from pykrx import stock, bond

# ===== 무위험 수익률 함수 =====
def get_risk_free_rate(date: str):
    try:
        df = bond.get_otc_treasury_yields(date)
        if df.empty:
            return None
        if "국고채 1년" in df.index:
            return float(df.loc["국고채 1년", "수익률"]) / 100
    except:
        return None

def get_risk_free_rate_series(start_date: str, end_date: str) -> pd.Series:
    start = datetime.strptime(start_date, "%Y%m%d")
    end = datetime.strptime(end_date, "%Y%m%d")
    dates = pd.date_range(start, end, freq="B")
    rf_list = []
    for date in dates:
        rate = get_risk_free_rate(date.strftime("%Y%m%d"))
        if rate is None:
            rf_list.append(None)
        else:
            daily_rate = (1 + rate) ** (1 / 252) - 1
            rf_list.append(daily_rate)
    rf_series = pd.Series(rf_list, index=dates)
    return rf_series.ffill()

# ===== 메인 =====
def main():
    base_dir = os.path.dirname(__file__)
    factor_path = sys.argv[1]
    code_path = sys.argv[2]

    with open(factor_path, "r", encoding="utf-8-sig") as f:
        factorDto = json.load(f)
    with open(code_path, "r", encoding="utf-8-sig") as f:
        stockCodeList = json.load(f)

    # 날짜 인덱스 정의
    date_index = pd.to_datetime([item["date"] for item in factorDto])
    min_date = date_index.min()
    max_date = date_index.max()
    start_date = min_date.strftime("%Y%m%d")
    end_date = max_date.strftime("%Y%m%d")
    lookback = 12

    # 팩터 공통 시리즈 생성
    factor_common = {
        "smb": pd.Series([item["smb"] for item in factorDto], index=date_index),
        "hml": pd.Series([item["hml"] for item in factorDto], index=date_index),
        "mom": pd.Series([item["mom"] for item in factorDto], index=date_index),
        "kospi": pd.Series([item["kospi"] for item in factorDto], index=date_index),
        "kosdaq": pd.Series([item["kosdaq"] for item in factorDto], index=date_index),
    }

    # 무위험 수익률 계산
    daily_rf = get_risk_free_rate_series(start_date, end_date)
    weekly_rf = daily_rf.resample("W-FRI").mean().dropna()

    recommendations = {}
    errors = {}

    for stock_info in stockCodeList:
        code = stock_info["stock_code"]
        market = stock_info["stock_market"].upper()

        try:
            print(f"\n=== 종목: {code} ===")

            price = stock.get_market_ohlcv_by_date(start_date, end_date, code)["종가"]
            print(f"[{code}] 종가 개수: {len(price)}")

            weekly_price = price.resample("W-FRI").last()
            print(f"[{code}] 리샘플된 주간 종가 개수: {len(weekly_price)}")

            weekly_return = weekly_price.pct_change()
            print(f"[{code}] 수익률 개수 (NaN 포함): {len(weekly_return)}")
            weekly_return = weekly_return.dropna()
            print(f"[{code}] 수익률 개수 (NaN 제거 후): {len(weekly_return)}")

            if weekly_return.empty:
                print(f"[{code}] 주간 수익률 데이터 없음")
                continue

            mkt_rf_series = factor_common["kospi"] if market == "KOSPI" else factor_common["kosdaq"]

            factor_df = pd.DataFrame({
                "mkt_rf": mkt_rf_series,
                "smb": factor_common["smb"],
                "hml": factor_common["hml"],
                "mom": factor_common["mom"]
            })
            weekly_factor = factor_df.resample("W-FRI").mean().dropna()

            # 공통 날짜로 정리
            common_dates = weekly_return.index.intersection(weekly_rf.index).intersection(weekly_factor.index)
            print(f"[{code}] 공통 인덱스 개수: {len(common_dates)}")

            y_full = (weekly_return.loc[common_dates] - weekly_rf.loc[common_dates]).dropna()
            X_full = weekly_factor.loc[common_dates].dropna()

            common_dates = y_full.index.intersection(X_full.index)
            y_full = y_full.loc[common_dates]
            X_full = X_full.loc[common_dates]

            if len(y_full) < 3:
                print(f"[{code}] 데이터 너무 적어서 회귀 불가")
                continue

            effective_lookback = min(lookback, len(y_full))

            result = []
            for i in range(effective_lookback, len(y_full) + 1):
                y = y_full.iloc[i - effective_lookback:i]
                X = X_full.iloc[i - effective_lookback:i]

                if y.isnull().any():
                    print(f"[{code}] y에 NaN 존재 → 건너뜀")
                    continue

                if X.isnull().any().any():
                    print(f"[{code}] X에 NaN 존재 → 건너뜀")
                    continue

                X = add_constant(X, has_constant='add')
                model = OLS(y, X).fit()

                alpha = model.params.get("const", np.nan)
                resid_std = np.std(model.resid)
                ir = alpha / resid_std if resid_std != 0 else 0

                result.append({
                    "date": y.index[-1].strftime("%Y-%m-%d"),
                    "alpha": alpha,
                    "ir": ir,
                    "r_squared": model.rsquared
                })

            if result:
                recommendations[code] = result
            else:
                print(f"[{code}] 유효한 결과 없음")

        except Exception as e:
            errors[code] = str(e)
            print(f"[{code}] 오류 발생: {e}")

    with open("error_log.json", "w", encoding="utf-8-sig") as f:
        json.dump(errors, f, ensure_ascii=False, indent=2)

    # 종목별 마지막 IR만 추출
    ir_result = {}
    for code, results in recommendations.items():
        if results:
            last_ir = results[-1].get("ir")
            if last_ir is not None:
                ir_result[code] = last_ir

    # IR 내림차순 정렬
    sorted_ir_result = dict(sorted(ir_result.items(), key=lambda x: x[1], reverse=True))

    outputPath = "./data/stock/output/calc_result.json"
    os.makedirs(os.path.dirname(outputPath), exist_ok=True)


    # 최종 결과 저장
    with open(outputPath, "w", encoding="utf-8-sig") as f:
        json.dump(sorted_ir_result, f, ensure_ascii=False, indent=2)

if __name__ == "__main__":
    main()
